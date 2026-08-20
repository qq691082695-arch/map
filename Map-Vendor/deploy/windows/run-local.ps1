param(
    [string]$JdkHome = 'C:\Users\18942\.jdks\corretto-1.8.0_432'
)

$ErrorActionPreference = 'Stop'
$javaExecutable = Join-Path $JdkHome 'bin\java.exe'
if (-not (Test-Path -LiteralPath $javaExecutable)) {
    throw "JDK 8 not found at $JdkHome"
}

$releaseFile = Join-Path $JdkHome 'release'
if (-not (Select-String -LiteralPath $releaseFile -Pattern 'JAVA_VERSION="1\.8\.' -Quiet)) {
    throw 'Map Vendor must run on JDK 8.'
}

$env:JAVA_HOME = $JdkHome
$env:SPRING_PROFILES_ACTIVE = 'local'

$backendRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$logDirectory = Join-Path $backendRoot 'logs'
New-Item -ItemType Directory -Path $logDirectory -Force | Out-Null
$startupLog = Join-Path $logDirectory ("startup-{0}.log" -f (Get-Date -Format 'yyyyMMdd-HHmmss'))

Write-Host "完整启动输出将保存到: $startupLog"
Start-Transcript -LiteralPath $startupLog -Force | Out-Null
try {
    Push-Location $backendRoot
    try {
        & (Join-Path $backendRoot 'mvnw.cmd') spring-boot:run
        $processExitCode = $LASTEXITCODE
    }
    finally {
        Pop-Location
    }
}
catch {
    Write-Error -ErrorRecord $_
    $processExitCode = 1
}
finally {
    Stop-Transcript | Out-Null
}

if ($processExitCode -ne 0) {
    Write-Error "后端启动失败（退出码 $processExitCode）。完整输出: $startupLog"
}
exit $processExitCode
