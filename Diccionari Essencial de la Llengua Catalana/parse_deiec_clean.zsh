#!/usr/bin/env zsh

INPUT="Diccionari Essencial de la Llengua Catalana.txt"
PARAULES="paraules.csv"
DEFS="definicions.csv"

echo "paraula" > "$PARAULES"
echo "paraula,definicio" > "$DEFS"

current_word=""
current_def=""

is_entry_line() {
    line="$1"

    # No línies buides
    [[ -z "$line" ]] && return 1

    # No números
    [[ "$line" == [0-9]* ]] && return 1

    # No símbols
    [[ "$line" == [\*\◆\■\(\']* ]] && return 1

    # No línies que comencen amb espai
    [[ "$line" == " "* ]] && return 1

    # Ha de començar amb una paraula alfabètica
    first_word="${line%% *}"
    [[ "$first_word" =~ ^[[:alpha:]àèéíïòóúüç·]+$ ]] || return 1

    return 0
}

flush_entry() {
    if [[ -n "$current_word" ]]; then
        echo "$current_word" >> "$PARAULES"
        safe_def=$(echo "$current_def" | tr ',' ';')
        echo "$current_word,$safe_def" >> "$DEFS"
    fi
}

while IFS= read -r line; do
    if is_entry_line "$line"; then
        flush_entry
        current_word="${line%% *}"
        current_def=""
    else
        current_def="$current_def $line"
    fi
done < "$INPUT"

flush_entry

echo "Fet! Generats: $PARAULES i $DEFS"
