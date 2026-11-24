import unittest

import tri_seuillage as t


def comp(nom="X", v=0, b=0, c=1, cov=100.0, s=0):
    return {"nom": nom, "vulnerabilites": v, "bugs_critiques": b,
            "complexite": c, "coverage": cov, "code_smells": s}


class ClassificationTest(unittest.TestCase):
    def test_une_vulnerabilite_est_urgente(self):
        self.assertEqual(t.URGENT, t.classer(comp(v=1)))

    def test_un_bug_critique_est_urgent(self):
        self.assertEqual(t.URGENT, t.classer(comp(b=1)))

    def test_la_securite_prime_meme_avec_de_bonnes_metriques(self):
        self.assertEqual(t.URGENT, t.classer(comp(v=1, c=1, cov=100.0)))

    def test_complexite_au_seuil_est_clean(self):
        self.assertEqual(t.CLEAN, t.classer(comp(c=10)))

    def test_complexite_au_dessus_du_seuil_est_a_surveiller(self):
        self.assertEqual(t.SURVEILLER, t.classer(comp(c=11)))

    def test_coverage_au_seuil_est_clean(self):
        self.assertEqual(t.CLEAN, t.classer(comp(cov=80.0)))

    def test_coverage_sous_le_seuil_est_a_surveiller(self):
        self.assertEqual(t.SURVEILLER, t.classer(comp(cov=79.9)))

    def test_composant_conforme_est_clean(self):
        self.assertEqual(t.CLEAN, t.classer(comp()))


class TriTest(unittest.TestCase):
    def test_la_securite_passe_avant_la_fiabilite(self):
        a = comp("a", v=1, b=0)
        b = comp("b", v=0, b=5)
        ordre = [x["nom"] for x in sorted([b, a], key=t.cle_de_tri)]
        self.assertEqual(["a", "b"], ordre)

    def test_la_fiabilite_passe_avant_la_complexite(self):
        a = comp("a", b=1, c=2)
        b = comp("b", b=0, c=50)
        ordre = [x["nom"] for x in sorted([b, a], key=t.cle_de_tri)]
        self.assertEqual(["a", "b"], ordre)

    def test_a_egalite_la_complexite_la_plus_haute_passe_en_premier(self):
        a = comp("a", c=30, cov=90.0)
        b = comp("b", c=12, cov=90.0)
        ordre = [x["nom"] for x in sorted([b, a], key=t.cle_de_tri)]
        self.assertEqual(["a", "b"], ordre)

    def test_a_egalite_la_couverture_la_plus_faible_passe_en_premier(self):
        a = comp("a", c=12, cov=70.0)
        b = comp("b", c=12, cov=40.0)
        ordre = [x["nom"] for x in sorted([a, b], key=t.cle_de_tri)]
        self.assertEqual(["b", "a"], ordre)

    def test_trois_categories_sont_toujours_presentes(self):
        resultat = t.trier_et_classer([comp(v=1)])
        self.assertEqual(t.CATEGORIES, list(resultat.keys()))
        self.assertEqual(1, len(resultat[t.URGENT]))
        self.assertEqual([], resultat[t.CLEAN])


class ValidationEtKpiTest(unittest.TestCase):
    def test_champ_manquant_refuse(self):
        mauvais = comp()
        del mauvais["coverage"]
        with self.assertRaises(ValueError):
            t.valider(mauvais)

    def test_valeur_non_numerique_refusee(self):
        with self.assertRaises(ValueError):
            t.valider(comp(c="beaucoup"))

    def test_valeur_negative_refusee(self):
        with self.assertRaises(ValueError):
            t.valider(comp(b=-1))

    def test_coverage_superieure_a_100_refusee(self):
        with self.assertRaises(ValueError):
            t.valider(comp(cov=120.0))

    def test_kpi_pourcentages(self):
        resultat = t.trier_et_classer([comp("a", v=1), comp("b"), comp("c"), comp("d", c=11)])
        k = t.kpi(resultat)
        self.assertEqual(4, k["total"])
        self.assertEqual(25.0, k[t.URGENT]["pourcentage"])
        self.assertEqual(25.0, k[t.SURVEILLER]["pourcentage"])
        self.assertEqual(50.0, k[t.CLEAN]["pourcentage"])


if __name__ == "__main__":
    unittest.main()