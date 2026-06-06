# Detailed check for long translations and potential issues

$baseDir = "C:\Users\fwony\Downloads\SPD\core\src\main\assets\messages"
$categories = @("actors", "items", "journal", "levels", "misc", "plants", "scenes", "ui", "windows")

$allLongItems = @()

foreach ($category in $categories) {
    $engFile = "$baseDir\$category\${category}.properties"
    $plFile = "$baseDir\$category\${category}_pl.properties"
    
    if ((Test-Path $engFile) -and (Test-Path $plFile)) {
        $engContent = Get-Content $engFile
        $plContent = Get-Content $plFile
        
        $engHash = @{}
        $plHash = @{}
        
        foreach ($line in $engContent) {
            if ($line -match '^([^=]+)=(.*)$') {
                $engHash[$matches[1]] = $matches[2]
            }
        }
        
        foreach ($line in $plContent) {
            if ($line -match '^([^=]+)=(.*)$') {
                $plHash[$matches[1]] = $matches[2]
            }
        }
        
        # Check all translations for length
        foreach ($key in $engHash.Keys) {
            if ($plHash.ContainsKey($key)) {
                $engVal = $engHash[$key]
                $plVal = $plHash[$key]
                
                $ratio = [math]::Round($plVal.Length / $engVal.Length, 2)
                
                if ($ratio -gt 1.5) {
                    $allLongItems += @{
                        Category = $category
                        Key = $key
                        EngLen = $engVal.Length
                        PlLen = $plVal.Length
                        Ratio = $ratio
                        EngVal = $engVal
                        PlVal = $plVal
                    }
                }
            }
        }
    }
}

# Sort by ratio descending
$allLongItems = $allLongItems | Sort-Object -Property Ratio -Descending

Write-Host "`nTOP LONG TRANSLATIONS (>150% of English):" -ForegroundColor Cyan
Write-Host "=" * 100

foreach ($item in $allLongItems | Select-Object -First 30) {
    Write-Host "`n[$($item.Category)] $($item.Key)" -ForegroundColor Yellow
    Write-Host "  Ratio: $($item.Ratio)x (EN: $($item.EngLen) chars → PL: $($item.PlLen) chars)"
    Write-Host "  EN: $($item.EngVal)"
    Write-Host "  PL: $($item.PlVal)"
}

Write-Host "`n`nTotal items >150% threshold: $($allLongItems.Count)" -ForegroundColor Cyan
