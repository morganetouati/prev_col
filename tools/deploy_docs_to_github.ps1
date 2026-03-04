# Deploy docs/privacy to gh-pages branch using git
# Usage: .\deploy_docs_to_github.ps1 -RepoUrl git@github.com:username/repo.git
param(
    [string]$RepoUrl = ''
)
if (-not $RepoUrl) { Write-Error "Please provide -RepoUrl"; exit 1 }
$Temp = "deploy_docs_temp"
if (Test-Path $Temp) { Remove-Item $Temp -Recurse -Force }
mkdir $Temp
git clone $RepoUrl $Temp
Push-Location $Temp
# create orphan branch and push only docs/privacy
git checkout --orphan gh-pages
git rm -rf .
cp -Recurse "../docs/privacy/." .
git add .
git commit -m "Deploy privacy page"
git push origin gh-pages --force
Pop-Location
Write-Host "Deployed docs/privacy to gh-pages branch on $RepoUrl"
