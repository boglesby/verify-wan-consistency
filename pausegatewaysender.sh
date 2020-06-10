. ./setenv.sh

gfsh -e "connect --locator=localhost[10331]" -e "pause gateway-sender --id=ny"
