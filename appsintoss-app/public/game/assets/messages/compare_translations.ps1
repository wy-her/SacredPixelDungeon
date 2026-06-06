# Script to compare English and Polish translation files

$baseDir = "C:\Users\fwony\Downloads\SPD\core\src\main\assets\messages"
$categories = @("actors", "items", "journal", "levels", "misc", "plants", "scenes", "ui", "windows")

foreach ($category in $categories) {
    $engFile = "$baseDir\$category\${category}.properties"
    $plFile = "$baseDir\$category\${category}_pl.properties"
    
    if ((Test-Path $engFile) -and (Test-Path $plFile)) {
        Write-Host "`n========== $category ==========" -ForegroundColor Cyan
        
        # Read files
        $engContent = Get-Content $engFile
        $plContent = Get-Content $plFile
        
        # Extract keys
        $engKeys = @()
        $plKeys = @()
        $engHash = @{}
        $plHash = @{}
        
        foreach ($line in $engContent) {
            if ($line -match '^([^=]+)=(.*)$') {
                $key = $matches[1]
                $value = $matches[2]
                $engKeys += $key
                $engHash[$key] = $value
            }
        }
        
        foreach ($line in $plContent) {
            if ($line -match '^([^=]+)=(.*)$') {
                $key = $matches[1]
                $value = $matches[2]
                $plKeys += $key
                $plHash[$key] = $value
            }
        }
        
        # 1. Missing keys in Polish
        $missingKeys = @()
        foreach ($key in $engKeys) {
            if ($plKeys -notcontains $key) {
                $missingKeys += $key
            }
        }
        
        if ($missingKeys.Count -gt 0) {
            Write-Host "MISSING KEYS IN POLISH: $($missingKeys.Count)" -ForegroundColor Red
            $missingKeys | ForEach-Object { Write-Host "  - $_" }
        }
        
        # 2. Extra keys in Polish
        $extraKeys = @()
        foreach ($key in $plKeys) {
            if ($engKeys -notcontains $key) {
                $extraKeys += $key
            }
        }
        
        if ($extraKeys.Count -gt 0) {
            Write-Host "EXTRA KEYS IN POLISH: $($extraKeys.Count)" -ForegroundColor Yellow
            $extraKeys | ForEach-Object { Write-Host "  - $_" }
        }
        
        # 3. Format string mismatch
        Write-Host "`nCHECKING FORMAT STRINGS..." -ForegroundColor Magenta
        $formatMismatches = @()
        foreach ($key in $engKeys) {
            if ($plKeys -contains $key) {
                $engVal = $engHash[$key]
                $plVal = $plHash[$key]
                
                # Extract format placeholders
                $engFormat = [regex]::Matches($engVal, '%[\d$]*[sdxf]') | ForEach-Object { $_.Value }
                $plFormat = [regex]::Matches($plVal, '%[\d$]*[sdxf]') | ForEach-Object { $_.Value }
                
                if (($engFormat | Measure-Object).Count -ne ($plFormat | Measure-Object).Count) {
                    $formatMismatches += @{Key=$key; EngCount=($engFormat | Measure-Object).Count; PlCount=($plFormat | Measure-Object).Count; EngVal=$engVal; PlVal=$plVal}
                }
                elseif ($engFormat.Count -gt 0) {
                    $engSorted = $engFormat | Sort-Object
                    $plSorted = $plFormat | Sort-Object
                    if (($engSorted | Measure-Object).Count -gt 0 -and (Compare-Object $engSorted $plSorted)) {
                        $formatMismatches += @{Key=$key; EngVal=$engVal; PlVal=$plVal; Note="Different format types/order"}
                    }
                }
            }
        }
        
        if ($formatMismatches.Count -gt 0) {
            Write-Host "FORMAT STRING MISMATCHES: $($formatMismatches.Count)" -ForegroundColor Red
            foreach ($mismatch in $formatMismatches) {
                Write-Host "  KEY: $($mismatch.Key)"
                Write-Host "    EN: $($mismatch.EngVal)"
                Write-Host "    PL: $($mismatch.PlVal)"
            }
        }
        
        # 4. Button length check (heuristic - look for UI strings)
        Write-Host "`nCHECKING TEXT LENGTH..." -ForegroundColor Magenta
        $tooLong = @()
        foreach ($key in $engKeys) {
            if ($plKeys -contains $key -and $key -match '(btn|button|label|title)' -or $category -eq "ui") {
                $engVal = $engHash[$key]
                $plVal = $plHash[$key]
                
                if ($plVal.Length -gt ($engVal.Length * 1.5)) {
                    $tooLong += @{Key=$key; EngLen=$engVal.Length; PlLen=$plVal.Length; EngVal=$engVal; PlVal=$plVal}
                }
            }
        }
        
        if ($tooLong.Count -gt 0) {
            Write-Host "POTENTIALLY LONG TRANSLATIONS (>150% of English): $($tooLong.Count)" -ForegroundColor Yellow
            foreach ($item in $tooLong) {
                if ($item.PlLen -gt 100) {  # Only show if reasonably long
                    Write-Host "  KEY: $($item.Key) (EN: $($item.EngLen) chars, PL: $($item.PlLen) chars)"
                    Write-Host "    EN: $($item.EngVal.Substring(0, [Math]::Min(60, $item.EngVal.Length)))..."
                    Write-Host "    PL: $($item.PlVal.Substring(0, [Math]::Min(60, $item.PlVal.Length)))..."
                }
            }
        }
    }
}
