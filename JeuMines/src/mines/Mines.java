package mines;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

// Source: http://zetcode.com/tutorials/javagamestutorial/minesweeper/

public class Mines extends JFrame {
    private static final long serialVersionUID = 4772165125287256837L;

    private final int WIDTH = 250;
    private final int WIDTH_MULTI = 340;
    private final int HEIGHT = 290;

    private JLabel statusbar;

    public Mines() {

        int joueurs = demanderNombreJoueurs();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(joueurs > 1 ? WIDTH_MULTI : WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setTitle(joueurs > 1 ? "Minesweeper - " + joueurs + " joueurs" : "Minesweeper");

        statusbar = new JLabel("");
        add(statusbar, BorderLayout.SOUTH);

        Board board = new Board(statusbar);
        if (joueurs > 1) {
            board.setPartie(new Partie(joueurs));
        }
        add(board);

        setResizable(false);
        setVisible(true);
    }

    private int demanderNombreJoueurs() {
        Object[] choix = {"1 joueur", "2 joueurs", "3 joueurs", "4 joueurs"};
        int reponse = JOptionPane.showOptionDialog(null, "Mode de jeu", "Minesweeper",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, choix, choix[0]);
        return reponse < 0 ? 1 : reponse + 1;
    }

    public static void main(String[] args) {
        new Mines();
    }
}
