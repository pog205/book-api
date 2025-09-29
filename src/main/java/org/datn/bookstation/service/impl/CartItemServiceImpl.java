package org.datn.bookstation.service.impl;

import org.datn.bookstation.dto.request.CartItemRequest;
import org.datn.bookstation.dto.request.BatchCartItemRequest;
import org.datn.bookstation.dto.request.SmartCartItemRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.CartItemResponse;
import org.datn.bookstation.entity.*;
import org.datn.bookstation.mapper.CartItemMapper;
import org.datn.bookstation.mapper.CartItemResponseMapper;
import org.datn.bookstation.repository.*;
import org.datn.bookstation.service.CartItemService;
import org.datn.bookstation.service.FlashSaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CartItemServiceImpl implements CartItemService {
    
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final CartItemMapper cartItemMapper;
    private final CartItemResponseMapper cartItemResponseMapper;
    private final FlashSaleService flashSaleService;
    private final CartRepository cartRepository;
    
    @Autowired
    private FlashSaleItemRepository flashSaleItemRepository;

    public CartItemServiceImpl(
            CartItemRepository cartItemRepository,
            BookRepository bookRepository,
            UserRepository userRepository,
            CartItemMapper cartItemMapper,
            CartItemResponseMapper cartItemResponseMapper,
            FlashSaleService flashSaleService,
            CartRepository cartRepository,
            FlashSaleItemRepository flashSaleItemRepository) {
        this.cartItemRepository = cartItemRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.cartItemMapper = cartItemMapper;
        this.cartItemResponseMapper = cartItemResponseMapper;
        this.flashSaleService = flashSaleService;
        this.cartRepository = cartRepository;
        this.flashSaleItemRepository = flashSaleItemRepository;
    }

    @Override
    public List<CartItemResponse> getCartItemsByUserId(Integer userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        return cartItems.stream()
                .map(cartItemResponseMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     *  ENHANCED: Thêm cart item với AUTO-DETECTION và validation toàn diện
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<CartItemResponse> addItemToCart(CartItemRequest request) {
        try {
            // 1. Validate user
            Optional<User> userOpt = userRepository.findById(request.getUserId());
            if (userOpt.isEmpty()) {
                return new ApiResponse<>(404, "User không tồn tại", null);
            }
            
            // 2. Validate book
            Optional<Book> bookOpt = bookRepository.findById(request.getBookId());
            if (bookOpt.isEmpty()) {
                return new ApiResponse<>(404, "Sách không tồn tại", null);
            }
            
            Book book = bookOpt.get();
            if (book.getStatus() != 1) {
                return new ApiResponse<>(400, "Sách đã ngừng bán", null);
            }
            
            // 3.  AUTO-DETECT: Tìm flash sale tốt nhất cho sách này
            FlashSaleItem flashSaleItem = null;
            String flashSaleMessage = "";
            Optional<FlashSaleItem> activeFlashSaleOpt = flashSaleService.findActiveFlashSaleForBook(request.getBookId().longValue());
            
            if (activeFlashSaleOpt.isPresent()) {
                FlashSaleItem candidate = activeFlashSaleOpt.get();
                flashSaleItem = candidate; //  FIX: Luôn sử dụng flash sale nếu có, để validate đúng
                flashSaleMessage = " 🔥 Đã áp dụng flash sale!";
            }
            
            // 4.  ENHANCED: Validate stock và flash sale limit với userId
            ApiResponse<String> stockValidation = validateStockWithUser(book, flashSaleItem, request.getQuantity(), request.getUserId());
            if (stockValidation.getStatus() != 200) {
                return new ApiResponse<>(stockValidation.getStatus(), stockValidation.getMessage(), null);
            }
            
            // 5. Get or create cart directly
            Cart cart = getOrCreateCart(request.getUserId());
            
            // 6.  SMART EXISTING ITEM DETECTION: Check by book first, then merge intelligently
            List<CartItem> existingItems = cartItemRepository.findExistingCartItemsByBook(
                cart.getId(), request.getBookId());
                
            CartItem cartItem;
            if (!existingItems.isEmpty()) {
                // Found existing item(s) for this book - smart merge
                cartItem = existingItems.get(0); // Get most recent item
                int newQuantity = cartItem.getQuantity() + request.getQuantity();
                
                //  ENHANCED: Re-validate stock và flash sale limit cho tổng số lượng mới
                ApiResponse<String> updateStockValidation = validateStockWithUser(book, flashSaleItem, newQuantity, request.getUserId());
                if (updateStockValidation.getStatus() != 200) {
                    return new ApiResponse<>(updateStockValidation.getStatus(), 
                        "Bạn đã có " + cartItem.getQuantity() + " trong giỏ. " + updateStockValidation.getMessage(), null);
                }
                
                //  SMART FLASH SALE UPDATE: Apply new flash sale if available
                if (flashSaleItem != null) {
                    cartItem.setFlashSaleItem(flashSaleItem);
                    flashSaleMessage = "  Đã áp dụng flash sale và cộng vào số lượng hiện có!";
                } else if (cartItem.getFlashSaleItem() != null) {
                    // Keep existing flash sale if new request doesn't have one
                    flashSaleMessage = "  Đã cộng vào số lượng hiện có (giữ flash sale cũ)!";
                } else {
                    flashSaleMessage = "  Đã cộng vào số lượng hiện có!";
                }
                
                cartItem.setQuantity(newQuantity);
                cartItem.setUpdatedBy(request.getUserId());
                cartItem.setUpdatedAt(System.currentTimeMillis());
                
                //  CLEANUP: Remove duplicate items if any
                for (int i = 1; i < existingItems.size(); i++) {
                    CartItem duplicate = existingItems.get(i);
                    cartItem.setQuantity(cartItem.getQuantity() + duplicate.getQuantity());
                    cartItemRepository.delete(duplicate);
                }
            } else {
                // Create new item
                cartItem = cartItemMapper.toEntity(request);
                cartItem.setCart(cart);
                cartItem.setBook(book);
                cartItem.setFlashSaleItem(flashSaleItem);
                cartItem.setCreatedBy(request.getUserId());
                cartItem.setCreatedAt(System.currentTimeMillis());
                cartItem.setUpdatedAt(System.currentTimeMillis());
            }
            
            CartItem savedItem = cartItemRepository.save(cartItem);
            CartItemResponse response = cartItemResponseMapper.toResponse(savedItem);
            
            return new ApiResponse<>(200, "Thêm sản phẩm vào giỏ hàng thành công" + flashSaleMessage, response);
            
        } catch (RuntimeException e) {
            log.error("Runtime error adding item to cart: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "Lỗi hệ thống: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("Unexpected error adding item to cart", e);
            return new ApiResponse<>(500, "Lỗi khi thêm sản phẩm: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<CartItemResponse> updateCartItem(Integer cartItemId, Integer quantity) {
        try {
            Optional<CartItem> cartItemOpt = cartItemRepository.findById(cartItemId);
            if (cartItemOpt.isEmpty()) {
                return new ApiResponse<>(404, "CartItem không tồn tại", null);
            }
            
            CartItem cartItem = cartItemOpt.get();
            
            // Validate quantity
            if (quantity <= 0) {
                // Delete cart item
                cartItemRepository.delete(cartItem);
                return new ApiResponse<>(200, "Xóa sản phẩm khỏi giỏ hàng thành công", null);
            }
            
            // Validate stock
            ApiResponse<String> stockValidation = validateStock(cartItem.getBook(), cartItem.getFlashSaleItem(), quantity);
            if (stockValidation.getStatus() != 200) {
                return new ApiResponse<>(stockValidation.getStatus(), stockValidation.getMessage(), null);
            }
            
            cartItem.setQuantity(quantity);
            cartItem.setUpdatedAt(System.currentTimeMillis());
            CartItem savedItem = cartItemRepository.save(cartItem);
            CartItemResponse response = cartItemResponseMapper.toResponse(savedItem);
            
            return new ApiResponse<>(200, "Cập nhật số lượng thành công", response);
            
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi cập nhật: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<String> removeCartItem(Integer cartItemId) {
        try {
            Optional<CartItem> cartItemOpt = cartItemRepository.findById(cartItemId);
            if (cartItemOpt.isEmpty()) {
                return new ApiResponse<>(404, "CartItem không tồn tại", null);
            }
            
            cartItemRepository.deleteById(cartItemId);
            return new ApiResponse<>(200, "Xóa sản phẩm khỏi giỏ hàng thành công", "OK");
            
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi xóa sản phẩm: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<List<CartItemResponse>> addItemsToCartBatch(BatchCartItemRequest request) {
        try {
            List<CartItemResponse> results = new ArrayList<>();
            
            for (CartItemRequest itemRequest : request.getItems()) {
                // Set userId từ batch request
                itemRequest.setUserId(request.getUserId());
                
                ApiResponse<CartItemResponse> addResult = addItemToCart(itemRequest);
                if (addResult.getStatus() == 200 && addResult.getData() != null) {
                    results.add(addResult.getData());
                }
                // Note: Có thể xử lý lỗi riêng lẻ hoặc fail toàn bộ batch
            }
            
            return new ApiResponse<>(200, "Thêm " + results.size() + "/" + request.getItems().size() + " sản phẩm thành công", results);
            
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi thêm batch: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<String> clearCartItems(Integer cartId) {
        try {
            cartItemRepository.deleteByCartId(cartId);
            return new ApiResponse<>(200, "Xóa toàn bộ items thành công", "OK");
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi xóa items: " + e.getMessage(), null);
        }
    }

    /**
     *  ENHANCED: Validate cart với nhiều business rules
     */
    @Override
    public ApiResponse<List<CartItemResponse>> validateAndUpdateCartItems(Integer userId) {
        try {
            long currentTime = System.currentTimeMillis();
            int totalUpdated = 0;
            List<String> warnings = new ArrayList<>();
            
            // 1. AUTO-UPDATE flash sale items status trước khi validate
            List<CartItem> expiredItems = cartItemRepository.findExpiredFlashSaleItems(userId, currentTime);
            for (CartItem item : expiredItems) {
                // Tự động update status của flash sale items dựa trên thời gian
                if (item.getFlashSaleItem() != null && item.getFlashSaleItem().getFlashSale() != null) {
                    FlashSale flashSale = item.getFlashSaleItem().getFlashSale();
                    Byte newStatus = (flashSale.getEndTime() > currentTime) ? (byte) 1 : (byte) 0;
                    
                    if (!newStatus.equals(item.getFlashSaleItem().getStatus())) {
                        item.getFlashSaleItem().setStatus(newStatus);
                        item.getFlashSaleItem().setUpdatedAt(currentTime);
                        item.getFlashSaleItem().setUpdatedBy(Long.valueOf(userId));
                        flashSaleItemRepository.save(item.getFlashSaleItem());
                        totalUpdated++;
                        
                        if (newStatus == 0) {
                            warnings.add("Flash sale \"" + item.getBook().getBookName() + "\" đã hết hạn, đã chuyển về giá gốc");
                        } else {
                            warnings.add("Flash sale \"" + item.getBook().getBookName() + "\" đã được gia hạn");
                        }
                    }
                }
            }
            
            // 2. Kiểm tra stock vượt quá
            List<CartItem> exceededItems = cartItemRepository.findCartItemsExceedingStock(userId);
            for (CartItem item : exceededItems) {
                int availableStock = item.getFlashSaleItem() != null ? 
                    item.getFlashSaleItem().getStockQuantity() : item.getBook().getStockQuantity();
                warnings.add("Sách \"" + item.getBook().getBookName() + "\" trong giỏ hàng (" + item.getQuantity() + ") vượt quá tồn kho (" + availableStock + ")");
            }
            
            // 3. Warning flash sale sắp hết hạn (còn 5 phút)
            long warningTime = currentTime + (5 * 60 * 1000); // 5 phút sau
            List<CartItem> aboutToExpireItems = cartItemRepository.findFlashSaleItemsAboutToExpire(currentTime, warningTime);
            for (CartItem item : aboutToExpireItems) {
                long remainingMinutes = (item.getFlashSaleItem().getFlashSale().getEndTime() - currentTime) / (60 * 1000);
                warnings.add("Flash sale \"" + item.getBook().getBookName() + "\" sẽ hết hạn trong " + remainingMinutes + " phút");
            }
            
            // 4. Lấy danh sách items mới nhất
            List<CartItemResponse> updatedItems = getCartItemsByUserId(userId);
            
            String message = buildValidationMessage(totalUpdated, warnings);
            return new ApiResponse<>(200, message, updatedItems);
            
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi validate giỏ hàng: " + e.getMessage(), null);
        }
    }

    @Override
    public CartItem getCartItemById(Integer cartItemId) {
        return cartItemRepository.findById(cartItemId).orElse(null);
    }

    @Override
    public boolean isCartItemBelongsToUser(Integer cartItemId, Integer userId) {
        Optional<CartItem> cartItemOpt = cartItemRepository.findById(cartItemId);
        if (cartItemOpt.isEmpty()) {
            return false;
        }
        
        CartItem cartItem = cartItemOpt.get();
        return cartItem.getCart() != null && 
               cartItem.getCart().getUser() != null && 
               cartItem.getCart().getUser().getId().equals(userId);
    }

    @Override
    @Transactional
    public ApiResponse<CartItemResponse> addSmartItemToCart(SmartCartItemRequest request) {
        // SmartCartItemRequest giờ đây chỉ là wrapper, logic auto-detect đã được tích hợp vào addItemToCart
        CartItemRequest cartItemRequest = CartItemRequest.builder()
                .userId(request.getUserId().intValue())
                .bookId(request.getBookId().intValue())
                .quantity(request.getQuantity())
                .build();
        
        // Gọi method add với auto-detect logic
        return addItemToCart(cartItemRequest);
    }

    @Override
    public int handleExpiredFlashSalesInCart() {
        try {
            long currentTime = System.currentTimeMillis();
            
            // Tìm tất cả cart items có flash sale đã hết hạn
            List<CartItem> expiredItems = cartItemRepository.findAllExpiredFlashSaleItems(currentTime);
            
            // AUTO-UPDATE status của flash sale items dựa trên thời gian
            for (CartItem item : expiredItems) {
                if (item.getFlashSaleItem() != null && item.getFlashSaleItem().getFlashSale() != null) {
                    FlashSale flashSale = item.getFlashSaleItem().getFlashSale();
                    Byte newStatus = (flashSale.getEndTime() > currentTime) ? (byte) 1 : (byte) 0;
                    
                    if (!newStatus.equals(item.getFlashSaleItem().getStatus())) {
                        item.getFlashSaleItem().setStatus(newStatus);
                        item.getFlashSaleItem().setUpdatedAt(currentTime);
                        item.getFlashSaleItem().setUpdatedBy(1L); // System user
                        flashSaleItemRepository.save(item.getFlashSaleItem());
                    }
                }
            }
            
            return expiredItems.size();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int handleExpiredFlashSaleInCart(Integer flashSaleId) {
        try {
            // Tìm cart items của flash sale cụ thể này
            List<CartItem> expiredItems = cartItemRepository.findByFlashSaleId(flashSaleId);
            
            // AUTO-UPDATE status dựa trên thời gian
            long currentTime = System.currentTimeMillis();
            for (CartItem item : expiredItems) {
                if (item.getFlashSaleItem() != null && item.getFlashSaleItem().getFlashSale() != null) {
                    FlashSale flashSale = item.getFlashSaleItem().getFlashSale();
                    Byte newStatus = (flashSale.getEndTime() > currentTime) ? (byte) 1 : (byte) 0;
                    
                    if (!newStatus.equals(item.getFlashSaleItem().getStatus())) {
                        item.getFlashSaleItem().setStatus(newStatus);
                        item.getFlashSaleItem().setUpdatedAt(currentTime);
                        item.getFlashSaleItem().setUpdatedBy(1L); // System user
                        flashSaleItemRepository.save(item.getFlashSaleItem());
                    }
                }
            }
            
            return expiredItems.size();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    /**
     *  DEPRECATED: Không còn cần batch update cart items khi flash sale hết hạn
     * Logic mới: Chỉ update status của FlashSaleItem, cart item giữ nguyên flashSaleItemId
     */
    @Deprecated
    public int handleExpiredFlashSalesInCartBatch(List<Integer> flashSaleIds) {
        try {
            if (flashSaleIds == null || flashSaleIds.isEmpty()) {
                return 0;
            }
            
            //  NEW LOGIC: Gọi FlashSaleService để update status thay vì set null cart item
            int totalUpdatedItems = 0;
            for (Integer flashSaleId : flashSaleIds) {
                try {
                    int updatedCount = flashSaleService.autoUpdateFlashSaleItemsStatus(flashSaleId);
                    totalUpdatedItems += updatedCount;
                } catch (Exception e) {
                    System.err.println(" ERROR: Failed to update status for flash sale " + flashSaleId + ": " + e.getMessage());
                }
            }
            
            // Log để tracking
            if (totalUpdatedItems > 0) {
                System.out.println(" BATCH EXPIRATION: Updated " + totalUpdatedItems + " flash sale items status for flash sales: " + flashSaleIds);
            }
            
            return totalUpdatedItems;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int syncCartItemsWithUpdatedFlashSale(Integer flashSaleId) {
        try {
            // Tìm flash sale item theo ID
            Optional<FlashSaleItem> flashSaleItemOpt = flashSaleService.findActiveFlashSaleForBook(null);
            
            // Nếu không tìm thấy active flash sale, skip
            if (flashSaleItemOpt.isEmpty()) {
                return 0;
            }
            
            FlashSaleItem flashSaleItem = flashSaleItemOpt.get();
            Long bookId = flashSaleItem.getBook().getId().longValue();
            
            // Tìm tất cả cart items của book này mà chưa có flash sale hoặc có flash sale khác
            List<CartItem> cartItemsToSync = cartItemRepository.findCartItemsForFlashSaleSync(bookId, flashSaleId);
            
            int syncCount = 0;
            for (CartItem item : cartItemsToSync) {
                // Validate stock trước khi sync
                if (item.getQuantity() <= flashSaleItem.getStockQuantity()) {
                    item.setFlashSaleItem(flashSaleItem);
                    item.setUpdatedAt(System.currentTimeMillis());
                    cartItemRepository.save(item);
                    syncCount++;
                }
            }
            
            System.out.println(" FLASH SALE SYNC: Updated " + syncCount + " cart items for flash sale " + flashSaleId);
            return syncCount;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int syncCartItemsWithNewFlashSale(Integer flashSaleId) {
        try {
            // Lấy tất cả flash sale items của flash sale này
            List<FlashSaleItem> flashSaleItems = flashSaleItemRepository.findByFlashSaleId(flashSaleId);
            if (flashSaleItems.isEmpty()) {
                return 0;
            }
            
            int totalSyncCount = 0;
            
            for (FlashSaleItem flashSaleItem : flashSaleItems) {
                // Chỉ sync nếu flash sale item đang active
                if (flashSaleItem.getStatus() != 1) {
                    continue;
                }
                
                Long bookId = flashSaleItem.getBook().getId().longValue();
                
                // Tìm cart items của book này mà chưa có flash sale item
                List<CartItem> cartItemsToSync = cartItemRepository.findCartItemsWithoutFlashSale(bookId);
                
                int syncCount = 0;
                for (CartItem item : cartItemsToSync) {
                    // Validate stock trước khi sync
                    if (item.getQuantity() <= flashSaleItem.getStockQuantity()) {
                        item.setFlashSaleItem(flashSaleItem);
                        item.setUpdatedAt(System.currentTimeMillis());
                        cartItemRepository.save(item);
                        syncCount++;
                    }
                }
                
                totalSyncCount += syncCount;
                log.info(" NEW FLASH SALE SYNC: Updated {} cart items for book {} in flash sale {}", 
                        syncCount, bookId, flashSaleId);
            }
            
            return totalSyncCount;
        } catch (Exception e) {
            log.error(" ERROR: syncCartItemsWithNewFlashSale failed for flash sale {}", flashSaleId, e);
            return 0;
        }
    }

    @Override
    public int mergeDuplicateCartItemsForUser(Integer userId) {
        try {
            // Tìm duplicate cart items (cùng book, cùng cart)
            List<CartItem> allItems = cartItemRepository.findByUserId(userId);
            java.util.Map<Integer, List<CartItem>> groupedByBook = allItems.stream()
                .collect(Collectors.groupingBy(item -> item.getBook().getId()));
            
            int mergedCount = 0;
            for (java.util.Map.Entry<Integer, List<CartItem>> entry : groupedByBook.entrySet()) {
                List<CartItem> itemsForBook = entry.getValue();
                
                if (itemsForBook.size() > 1) {
                    // Có duplicate - merge vào item đầu tiên
                    CartItem primaryItem = itemsForBook.get(0);
                    int totalQuantity = primaryItem.getQuantity();
                    
                    // Merge quantity từ các items khác
                    for (int i = 1; i < itemsForBook.size(); i++) {
                        CartItem duplicateItem = itemsForBook.get(i);
                        totalQuantity += duplicateItem.getQuantity();
                        
                        // Giữ flash sale tốt nhất (có flash sale > không có)
                        if (primaryItem.getFlashSaleItem() == null && duplicateItem.getFlashSaleItem() != null) {
                            primaryItem.setFlashSaleItem(duplicateItem.getFlashSaleItem());
                        }
                        
                        cartItemRepository.delete(duplicateItem);
                        mergedCount++;
                    }
                    
                    primaryItem.setQuantity(totalQuantity);
                    primaryItem.setUpdatedAt(System.currentTimeMillis());
                    cartItemRepository.save(primaryItem);
                }
            }
            
            System.out.println(" CLEANUP: Merged " + mergedCount + " duplicate cart items for user " + userId);
            return mergedCount;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    // ================== PRIVATE HELPER METHODS ==================
    
    /**
     *  ENHANCED: Validate stock và flash sale limit cho book hoặc flash sale
     */
    private ApiResponse<String> validateStock(Book book, FlashSaleItem flashSaleItem, Integer requestedQuantity) {
        return validateStockWithUser(book, flashSaleItem, requestedQuantity, null);
    }
    
    /**
     *  ENHANCED: Validate stock và flash sale limit với user validation
     */
    private ApiResponse<String> validateStockWithUser(Book book, FlashSaleItem flashSaleItem, Integer requestedQuantity, Integer userId) {
        if (flashSaleItem != null) {
            // 1. Validate flash sale stock
            if (requestedQuantity > flashSaleItem.getStockQuantity()) {
                return new ApiResponse<>(400, "Flash sale không đủ hàng. Còn lại: " + flashSaleItem.getStockQuantity(), null);
            }
            
            // 2.  ENHANCED: Validate flash sale purchase limit per user với hai loại thông báo
            if (userId != null && flashSaleItem.getMaxPurchasePerUser() != null) {
                if (!flashSaleService.canUserPurchaseMore(flashSaleItem.getId().longValue(), userId, requestedQuantity)) {
                    int currentPurchased = flashSaleService.getUserPurchasedQuantity(flashSaleItem.getId().longValue(), userId);
                    int maxAllowed = flashSaleItem.getMaxPurchasePerUser();
                
                    //  LOẠI 1: Đã đạt giới hạn tối đa, không thể mua nữa
                    if (currentPurchased >= maxAllowed) {
                        return new ApiResponse<>(400, String.format(
                            "Bạn đã mua đủ %d sản phẩm flash sale '%s' cho phép. Không thể thêm vào giỏ hàng.", 
                            maxAllowed, book.getBookName()), null);
                    }
                    
                    //  LOẠI 2: Chưa đạt giới hạn nhưng đặt quá số lượng cho phép
                    int remainingAllowed = maxAllowed - currentPurchased;
                    if (requestedQuantity > remainingAllowed) {
                        return new ApiResponse<>(400, String.format(
                            "Bạn đã mua %d sản phẩm, chỉ được mua thêm tối đa %d sản phẩm flash sale '%s'.", 
                            currentPurchased, remainingAllowed, book.getBookName()), null);
                    }
                    
                    //  LOẠI 3: Thông báo chung
                    return new ApiResponse<>(400, String.format(
                        "Bạn chỉ được mua tối đa %d sản phẩm flash sale '%s'.", 
                        maxAllowed, book.getBookName()), null);
                }
            }
        } else {
            // Using regular book stock  
            if (requestedQuantity > book.getStockQuantity()) {
                return new ApiResponse<>(400, "Không đủ hàng tồn kho. Còn lại: " + book.getStockQuantity(), null);
            }
        }
        return new ApiResponse<>(200, "Stock OK", "OK");
    }
    
    /**
     * Get or create cart for user
     */
    private Cart getOrCreateCart(Integer userId) {
        try {
            log.debug("Getting or creating cart for user: {}", userId);
            
            Optional<Cart> cartOpt = cartRepository.findActiveCartByUserId(userId);
            
            if (cartOpt.isPresent()) {
                log.debug("Found existing cart for user: {}", userId);
                return cartOpt.get();
            }
            
            // Tạo cart mới
            log.debug("Creating new cart for user: {}", userId);
            
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                log.error("User not found with id: {}", userId);
                throw new RuntimeException("User not found with id: " + userId);
            }
            
            User user = userOpt.get();
            Cart newCart = new Cart();
            newCart.setUser(user);
            newCart.setCreatedAt(System.currentTimeMillis());
            newCart.setCreatedBy(userId);
            newCart.setStatus((byte) 1); // Active
            
            Cart savedCart = cartRepository.save(newCart);
            log.debug("Created new cart with id: {} for user: {}", savedCart.getId(), userId);
            
            return savedCart;
        } catch (Exception e) {
            log.error("Error getting or creating cart for user: {}", userId, e);
            throw new RuntimeException("Error getting or creating cart: " + e.getMessage(), e);
        }
    }
    
    /**
     * Build validation message từ results
     */
    private String buildValidationMessage(int updatedCount, List<String> warnings) {
        if (updatedCount == 0 && warnings.isEmpty()) {
            return "Giỏ hàng đã được kiểm tra - Không có vấn đề";
        }
        
        StringBuilder message = new StringBuilder();
        if (updatedCount > 0) {
            message.append("Đã cập nhật ").append(updatedCount).append(" sản phẩm flash sale hết hạn. ");
        }
        if (!warnings.isEmpty()) {
            message.append("Cảnh báo: ").append(String.join("; ", warnings));
        }
        
        return message.toString();
    }

    @Override
    public ApiResponse<CartItemResponse> updateCartItemSelected(Integer cartItemId, Boolean selected) {
        try {
            Optional<CartItem> cartItemOpt = cartItemRepository.findById(cartItemId);
            if (cartItemOpt.isEmpty()) {
                return new ApiResponse<>(404, "CartItem không tồn tại", null);
            }
            CartItem cartItem = cartItemOpt.get();
            cartItem.setSelected(selected);
            cartItem.setUpdatedAt(System.currentTimeMillis());
            CartItem savedItem = cartItemRepository.save(cartItem);
            CartItemResponse response = cartItemResponseMapper.toResponse(savedItem);
            return new ApiResponse<>(200, "Cập nhật trạng thái chọn/bỏ thành công", response);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi cập nhật trạng thái chọn/bỏ: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<CartItemResponse> toggleCartItemSelected(Integer cartItemId) {
        try {
            Optional<CartItem> cartItemOpt = cartItemRepository.findById(cartItemId);
            if (cartItemOpt.isEmpty()) {
                return new ApiResponse<>(404, "CartItem không tồn tại", null);
            }
            CartItem cartItem = cartItemOpt.get();
            cartItem.setSelected(!Boolean.TRUE.equals(cartItem.getSelected()));
            cartItem.setUpdatedAt(System.currentTimeMillis());
            CartItem savedItem = cartItemRepository.save(cartItem);
            CartItemResponse response = cartItemResponseMapper.toResponse(savedItem);
            return new ApiResponse<>(200, "Đã đảo trạng thái chọn/bỏ thành công", response);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi đảo trạng thái chọn/bỏ: " + e.getMessage(), null);
        }
    }
}
