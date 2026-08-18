param(
    [Parameter(Mandatory = $true)][string]$FlywayPassword,
    [string]$JdkHome = 'C:\Users\18942\.jdks\corretto-1.8.0_432'
)

$ErrorActionPreference = 'Stop'
$env:JAVA_HOME = $JdkHome
$env:MAP_VENDOR_FLYWAY_PASSWORD = $FlywayPassword
$env:MAP_VENDOR_DB_URL = 'jdbc:mysql://127.0.0.1:3306/map_vendor?useUnicode=true&characterEncoding=utf8&connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true'
$env:MAP_VENDOR_FLYWAY_USERNAME = 'map_vendor_flyway'

& (Join-Path $PSScriptRoot '..\..\mvnw.cmd') --batch-mode flyway:migrate flyway:validate
if ($LASTEXITCODE -ne 0) {
    throw 'Flyway migration verification failed.'
}
