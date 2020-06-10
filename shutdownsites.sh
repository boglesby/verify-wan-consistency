. ./setenv.sh

gfsh -e "connect --locator=localhost[10331]" -e "shutdown --include-locators=true"
gfsh -e "connect --locator=localhost[10332]" -e "shutdown --include-locators=true"
