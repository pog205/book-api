# Debug order status transition issue
Write-Host "Debug order status transition..." -ForegroundColor Cyan

# Get current order to see actual status
$orderId = 3
$getOrderUrl = "http://localhost:8080/api/orders/$orderId"

try {
    Write-Host "Getting order $orderId details..." -ForegroundColor Blue
    $orderResp = Invoke-RestMethod -Uri $getOrderUrl -Method Get -ContentType "application/json"
    $order = $orderResp.data
    
    Write-Host "`nOrder Details:" -ForegroundColor Yellow
    Write-Host "ID: $($order.id)" -ForegroundColor White
    Write-Host "Code: $($order.code)" -ForegroundColor White  
    Write-Host "Status: $($order.orderStatus)" -ForegroundColor Green
    Write-Host "Type: $($order.orderType)" -ForegroundColor White
    Write-Host "Subtotal: $($order.subtotal)" -ForegroundColor White
    
    # Try transition with correct current status
    $transitionUrl = "http://localhost:8080/api/orders/$orderId/status-transition"
    $transitionData = @{
        orderId = $orderId
        currentStatus = $order.orderStatus
        newStatus = "CONFIRMED"
        performedBy = 1
        reason = "Debug test transition"
    } | ConvertTo-Json
    
    Write-Host "`nTrying transition with correct currentStatus: $($order.orderStatus)..." -ForegroundColor Blue
    $transitionResp = Invoke-RestMethod -Uri $transitionUrl -Method Post -Body $transitionData -ContentType "application/json"
    
    Write-Host "Transition successful!" -ForegroundColor Green
    Write-Host "Response: $($transitionResp | ConvertTo-Json -Depth 3)" -ForegroundColor Gray
    
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails) {
        Write-Host "Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

Write-Host "`nPress Enter to exit..."
Read-Host
