from wordfreq import word_frequency
import csv
import os

# Directori on tens tots els diccionaris
BASE = os.path.expanduser("~/Documents/juego/eWordle-main/eWordle-main/src/Word Sources")

def calcula_dificultat(freq):
    if freq > 1e-4:
        return 1
    elif freq > 1e-6:
        return 2
    else:
        return 3

# Recorre tots els fitxers .csv del directori
for filename in os.listdir(BASE):
    if not filename.endswith(".csv"):
        continue

    input_path = os.path.join(BASE, filename)
    output_path = os.path.join(BASE, filename.replace(".csv", "_dificultat.csv"))

    print(f"Processant {filename}...")

    with open(input_path, "r", encoding="utf-8") as f_in, \
         open(output_path, "w", encoding="utf-8", newline="") as f_out:

        reader = csv.reader(f_in)
        writer = csv.writer(f_out)

        # Escriu capçalera
        writer.writerow(["paraula", "dificultat"])

        next(reader)  # salta la capçalera original

        for row in reader:
            paraula = row[0].strip()

            # Calcula freqüència
            freq = word_frequency(paraula, "ca")

            # Assigna dificultat
            dificultat = calcula_dificultat(freq)

            # Escriu resultat
            writer.writerow([paraula, dificultat])

    print(f" → Creat: {output_path}")

print("Fet! Tots els diccionaris han estat processats.")
