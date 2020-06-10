. ./setenv.sh

gfsh -e "connect --locator=localhost[10331]" -e "resume gateway-sender --id=ny"
