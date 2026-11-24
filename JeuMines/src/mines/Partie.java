package mines;

import java.util.ArrayList;
import java.util.List;

/**
 * Regles du mode multijoueur (2 a 4 joueurs, chacun son tour sur le meme ecran).
 * Un coup = un clic gauche qui decouvre au moins une case.
 * Chaque case decouverte rapporte 1 point au joueur qui a joue.
 * Le joueur qui tombe sur une mine perd ; parmi les autres, le meilleur score gagne.
 * Si toutes les cases sures sont decouvertes sans mine, le meilleur score gagne.
 * Cette classe ne contient aucun code d'affichage : elle se teste directement.
 */
public class Partie {

    public static final int MIN_JOUEURS = 2;
    public static final int MAX_JOUEURS = 4;

    private final int nombreJoueurs;
    private final int[] scores;
    private int joueurCourant;
    private int perdant;
    private boolean terminee;

    public Partie(int nombreJoueurs) {
        if (nombreJoueurs < MIN_JOUEURS || nombreJoueurs > MAX_JOUEURS) {
            throw new IllegalArgumentException(
                "Le nombre de joueurs doit etre entre " + MIN_JOUEURS + " et " + MAX_JOUEURS);
        }
        this.nombreJoueurs = nombreJoueurs;
        this.scores = new int[nombreJoueurs];
        reinitialiser();
    }

    public void reinitialiser() {
        for (int i = 0; i < nombreJoueurs; i++) {
            scores[i] = 0;
        }
        joueurCourant = 0;
        perdant = -1;
        terminee = false;
    }

    public void enregistrerCoup(int casesDecouvertes, boolean mineTouchee, boolean toutDecouvert) {
        if (terminee) {
            return;
        }
        scores[joueurCourant] += casesDecouvertes;
        if (mineTouchee) {
            perdant = joueurCourant;
            terminee = true;
        } else if (toutDecouvert) {
            terminee = true;
        } else {
            joueurCourant = (joueurCourant + 1) % nombreJoueurs;
        }
    }

    public int getNombreJoueurs() {
        return nombreJoueurs;
    }

    public int getJoueurCourant() {
        return joueurCourant;
    }

    public int getScore(int joueur) {
        return scores[joueur];
    }

    public int getPerdant() {
        return perdant;
    }

    public boolean estTerminee() {
        return terminee;
    }

    public List<Integer> gagnants() {
        List<Integer> resultat = new ArrayList<>();
        if (!terminee) {
            return resultat;
        }
        int meilleur = -1;
        for (int i = 0; i < nombreJoueurs; i++) {
            if (i != perdant && scores[i] > meilleur) {
                meilleur = scores[i];
            }
        }
        for (int i = 0; i < nombreJoueurs; i++) {
            if (i != perdant && scores[i] == meilleur) {
                resultat.add(i);
            }
        }
        return resultat;
    }

    public String resume(int minesRestantes) {
        if (!terminee) {
            return "Tour J" + (joueurCourant + 1) + " | " + scoresEnTexte() + " | Mines " + minesRestantes;
        }
        if (perdant >= 0) {
            return "J" + (perdant + 1) + " saute sur une mine | " + verdict();
        }
        return "Fin | " + verdict() + " | " + scoresEnTexte();
    }

    private String verdict() {
        List<Integer> g = gagnants();
        if (g.size() == 1) {
            return "Gagnant : J" + (g.get(0) + 1);
        }
        StringBuilder sb = new StringBuilder("Egalite : ");
        for (int i = 0; i < g.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("J").append(g.get(i) + 1);
        }
        return sb.toString();
    }

    private String scoresEnTexte() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nombreJoueurs; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append("J").append(i + 1).append(":").append(scores[i]);
        }
        return sb.toString();
    }
}
