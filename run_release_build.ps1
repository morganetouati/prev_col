Set-Location "c:\Users\morgane\Desktop\prev_col"
$process = Start-Process -FilePath "cmd.exe" -ArgumentList '/c "cd /d c:\Users\morgane\Desktop\prev_col & call gradlew.bat :app:bundleRelease --no-daemon --console=plain"' -RedirectStandardOutput "c:\Users\morgane\Desktop\prev_col\build_v10_stdout.txt" -RedirectStandardError "c:\Users\morgane\Desktop\prev_col\build_v10_stderr.txt" -PassThru -Wait
$process.ExitCode | Out-File "c:\Users\morgane\Desktop\prev_col\build_v10_exitcode.txt"
