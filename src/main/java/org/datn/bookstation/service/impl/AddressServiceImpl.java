package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.request.AddressRequest;
import org.datn.bookstation.dto.response.AddressResponse;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.entity.Address;
import org.datn.bookstation.entity.User;
import org.datn.bookstation.mapper.AddressMapper;
import org.datn.bookstation.repository.AddressRepository;
import org.datn.bookstation.repository.UserRepository;
import org.datn.bookstation.service.AddressService;
import org.datn.bookstation.specification.AddressSpecification;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    @Override
    public ApiResponse<List<AddressResponse>> getAddressesByUser(Integer userId) {
        try {
            // Lấy theo Specification, chỉ lấy status=1
            var spec = AddressSpecification.filterBy(userId, (byte) 1);
            List<Address> addresses = addressRepository.findAll(spec, Sort.by(Sort.Order.desc("isDefault"), Sort.Order.desc("createdAt")));
            List<AddressResponse> responses = addresses.stream()
                    .map(addressMapper::toResponse)
                    .collect(Collectors.toList());
            return new ApiResponse<>(200, "Lấy danh sách địa chỉ thành công", responses);
        } catch (Exception e) {
            log.error("Error getAddressesByUser", e);
            return new ApiResponse<>(500, "Lỗi khi lấy địa chỉ: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<AddressResponse> getById(Integer id) {
        try {
            Address address = addressRepository.findById(id).orElse(null);
            if (address == null) {
                return new ApiResponse<>(404, "Không tìm thấy địa chỉ", null);
            }
            return new ApiResponse<>(200, "Lấy địa chỉ thành công", addressMapper.toResponse(address));
        } catch (Exception e) {
            log.error("Error getAddressById", e);
            return new ApiResponse<>(500, "Lỗi khi lấy địa chỉ: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<AddressResponse> create(AddressRequest request) {
        try {
            User user = userRepository.findById(request.getUserId()).orElse(null);
            if (user == null) {
                return new ApiResponse<>(404, "Người dùng không tồn tại", null);
            }

            Address address = addressMapper.toAddress(request);
            address.setUser(user);
            address.setCreatedBy(request.getUserId());
            address.setStatus((byte) 1);

            Address saved = addressRepository.save(address);

            // Nếu chưa set addressType thì mặc định HOME
            if (saved.getAddressType() == null) {
                saved.setAddressType(org.datn.bookstation.entity.enums.AddressType.HOME);
                saved = addressRepository.save(saved);
            }

            // Nếu set default thì bỏ default ở address khác
            updateDefaultAddress(saved);

            return new ApiResponse<>(201, "Tạo địa chỉ thành công", addressMapper.toResponse(saved));
        } catch (Exception e) {
            log.error("Error createAddress", e);
            return new ApiResponse<>(500, "Lỗi khi tạo địa chỉ: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<AddressResponse> update(AddressRequest request, Integer id) {
        try {
            Address existing = addressRepository.findById(id).orElse(null);
            if (existing == null) {
                return new ApiResponse<>(404, "Không tìm thấy địa chỉ", null);
            }

            // Cập nhật user nếu cần
            if (request.getUserId() != null && !request.getUserId().equals(existing.getUser().getId())) {
                User user = userRepository.findById(request.getUserId()).orElse(null);
                if (user == null) {
                    return new ApiResponse<>(404, "Người dùng không tồn tại", null);
                }
                existing.setUser(user);
            }

            existing.setRecipientName(request.getRecipientName());
            existing.setAddressDetail(request.getAddressDetail());
            existing.setPhoneNumber(request.getPhoneNumber());
            existing.setProvinceName(request.getProvinceName());
            existing.setProvinceId(request.getProvinceId());
            existing.setDistrictName(request.getDistrictName());
            existing.setDistrictId(request.getDistrictId());
            existing.setWardName(request.getWardName());
            existing.setWardCode(request.getWardCode());
            existing.setIsDefault(request.getIsDefault());

            if (request.getAddressType() != null) {
                existing.setAddressType(request.getAddressType());
            }
            existing.setUpdatedBy(request.getUserId());
            existing.setUpdatedAt(System.currentTimeMillis());

            Address saved = addressRepository.save(existing);

            // Nếu set default thì bỏ default ở address khác
            updateDefaultAddress(saved);

            return new ApiResponse<>(200, "Cập nhật địa chỉ thành công", addressMapper.toResponse(saved));
        } catch (Exception e) {
            log.error("Error updateAddress", e);
            return new ApiResponse<>(500, "Lỗi khi cập nhật địa chỉ: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> delete(Integer id) {
        try {
            Address address = addressRepository.findById(id).orElse(null);
            if (address == null) {
                return new ApiResponse<>(404, "Không tìm thấy địa chỉ", null);
            }

            Integer userId = address.getUser().getId();
            boolean isDefault = Boolean.TRUE.equals(address.getIsDefault());

            addressRepository.deleteById(id);

            // Nếu vừa xoá địa chỉ mặc định, tự động gán 1 địa chỉ khác làm mặc định
            if (isDefault) {
                List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDesc(userId);
                if (!addresses.isEmpty()) {
                    Address first = addresses.get(0);
                    if (!Boolean.TRUE.equals(first.getIsDefault())) {
                        first.setIsDefault(true);
                        addressRepository.save(first);
                    }
                }
            }

            return new ApiResponse<>(200, "Xóa địa chỉ thành công", null);
        } catch (Exception e) {
            log.error("Error deleteAddress", e);
            return new ApiResponse<>(500, "Lỗi khi xoá địa chỉ: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> disable(Integer id) {
        try {
            Address address = addressRepository.findById(id).orElse(null);
            if (address == null) {
                return new ApiResponse<>(404, "Không tìm thấy địa chỉ", null);
            }
            if (address.getStatus() != null && address.getStatus() == 0) {
                return new ApiResponse<>(400, "Địa chỉ đã bị xoá trước đó", null);
            }
            address.setIsDefault(false);
            address.setStatus((byte) 0);
            addressRepository.save(address);

            Integer userId = address.getUser().getId();
            boolean isDefault = Boolean.TRUE.equals(address.getIsDefault());


            // Nếu vừa xoá địa chỉ mặc định, tự động gán 1 địa chỉ khác làm mặc định
            if (isDefault) {
                List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDesc(userId);
                if (!addresses.isEmpty()) {
                    Address first = addresses.get(0);
                    if (!Boolean.TRUE.equals(first.getIsDefault())) {
                        first.setIsDefault(true);
                        addressRepository.save(first);
                    }
                }
            }

            
            return new ApiResponse<>(200, "Đã xoá địa chỉ thành công", null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi xoá địa chỉ: " + e.getMessage(), null);
        }
    }

    /**
     * Đảm bảo chỉ có tối đa 1 địa chỉ mặc định cho mỗi user.
     */
    private void updateDefaultAddress(Address newOrUpdatedAddress) {
        if (Boolean.TRUE.equals(newOrUpdatedAddress.getIsDefault())) {
            List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDesc(newOrUpdatedAddress.getUser().getId());
            addresses.stream()
                    .filter(a -> !a.getId().equals(newOrUpdatedAddress.getId()) && Boolean.TRUE.equals(a.getIsDefault()))
                    .forEach(a -> {
                        a.setIsDefault(false);
                        addressRepository.save(a);
                    });
        }
    }
} 