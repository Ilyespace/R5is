import json
import sys

SEUIL_COMPLEXITE = 10      
SEUIL_COVERAGE = 80.0      


URGENT = "A refactorer d'urgence"
SURVEILLER = "A surveiller"
CLEAN = "Clean"
CATEGORIES = [URGENT, SURVEILLER, CLEAN]

CHAMPS = ["vulnerabilites", "bugs_critiques", "complexite", "coverage", "code_smells"]



def valider(composant):
    if "nom" not in composant:
        raise ValueError("composant sans champ 'nom'")
    nom = composant["nom"]
    for champ in CHAMPS:
        if champ not in composant:
            raise ValueError(f"{nom} : champ manquant '{champ}'")
        valeur = composant[champ]
        if isinstance(valeur, bool) or not isinstance(valeur, (int, float)):
            raise ValueError(f"{nom} : '{champ}' doit etre un nombre")
        if valeur < 0:
            raise ValueError(f"{nom} : '{champ}' ne peut pas etre negatif")
    if composant["coverage"] > 100:
        raise ValueError(f"{nom} : 'coverage' doit etre entre 0 et 100")


def charger(chemin):
    with open(chemin, encoding="utf-8") as fichier:
        donnees = json.load(fichier)
    composants = donnees["composants"] if isinstance(donnees, dict) else donnees
    for composant in composants:
        valider(composant)
    return composants


def classer(composant):
    """Applique les regles metier dans l'ordre de la boussole des priorites."""
    if composant["vulnerabilites"] > 0 or composant["bugs_critiques"] > 0:
        return URGENT
    if composant["complexite"] > SEUIL_COMPLEXITE or composant["coverage"] < SEUIL_COVERAGE:
        return SURVEILLER
    return CLEAN


def raisons(composant):
    """Explique quels seuils sont depasses (utile pour piloter par KPI)."""
    liste = []
    if composant["vulnerabilites"] > 0:
        liste.append(f"{composant['vulnerabilites']} vulnerabilite(s)")
    if composant["bugs_critiques"] > 0:
        liste.append(f"{composant['bugs_critiques']} bug(s) critique(s)")
    if composant["complexite"] > SEUIL_COMPLEXITE:
        liste.append(f"complexite {composant['complexite']} > {SEUIL_COMPLEXITE}")
    if composant["coverage"] < SEUIL_COVERAGE:
        liste.append(f"coverage {composant['coverage']:.1f} < {SEUIL_COVERAGE}")
    return liste


def cle_de_tri(composant):
    """Tri multicritere : Securite > Fiabilite > Complexite > Couverture > Smells.

    Le signe moins trie par ordre decroissant (le pire en premier) ;
    la couverture est triee par ordre croissant (la plus faible en premier).
    """
    return (
        -composant["vulnerabilites"],
        -composant["bugs_critiques"],
        -composant["complexite"],
        composant["coverage"],
        -composant["code_smells"],
        composant["nom"],
    )


def trier_et_classer(composants):
    resultat = {categorie: [] for categorie in CATEGORIES}
    for composant in sorted(composants, key=cle_de_tri):
        resultat[classer(composant)].append(composant)
    return resultat


def kpi(resultat):
    total = sum(len(liste) for liste in resultat.values())
    indicateurs = {"total": total}
    for categorie, liste in resultat.items():
        indicateurs[categorie] = {
            "nombre": len(liste),
            "pourcentage": round(100 * len(liste) / total, 1) if total else 0.0,
        }
    return indicateurs


def afficher(resultat):
    for categorie in CATEGORIES:
        liste = resultat[categorie]
        print(f"\n=== {categorie} ({len(liste)}) ===")
        if not liste:
            print("  (aucun composant)")
        for c in liste:
            detail = "; ".join(raisons(c)) or "conforme au SonarWay"
            print(f"  {c['nom']:<30} vuln={c['vulnerabilites']} bugs={c['bugs_critiques']} "
                  f"cplx={c['complexite']:<3} cov={c['coverage']:5.1f}% smells={c['code_smells']}  | {detail}")
    indicateurs = kpi(resultat)
    print("\n=== KPI ===")
    print(f"  Composants analyses : {indicateurs['total']}")
    for categorie in CATEGORIES:
        k = indicateurs[categorie]
        print(f"  {categorie:<24} {k['nombre']:>3}  ({k['pourcentage']} %)")


def main(argv):
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")
    if len(argv) != 2:
        print("Usage : python tri_seuillage.py composants.json")
        return 2
    try:
        composants = charger(argv[1])
    except (OSError, ValueError, KeyError) as erreur:
        print(f"Erreur : {erreur}")
        return 1
    resultat = trier_et_classer(composants)
    afficher(resultat)
    with open("resultat.json", "w", encoding="utf-8") as sortie:
        json.dump({"kpi": kpi(resultat),
                   "categories": {c: [x["nom"] for x in resultat[c]] for c in CATEGORIES}},
                  sortie, ensure_ascii=False, indent=2)
    print("\nResultat ecrit dans resultat.json")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))