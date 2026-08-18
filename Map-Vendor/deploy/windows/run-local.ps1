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
& (Join-Path $PSScriptRoot '..\..\mvnw.cmd') spring-boot:run
