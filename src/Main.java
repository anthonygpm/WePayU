import br.ufal.ic.p2.wepayu.Facade;
import easyaccept.EasyAccept;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        String facade = "br.ufal.ic.p2.wepayu.Facade";

        EasyAccept.main(new String[]{
                facade,
                "tests/us1.txt",
                "tests/us1_1.txt",
                "tests/us2.txt",
                "tests/us2_1.txt",
                "tests/us3.txt",
                "tests/us3_1.txt",
                "tests/us4.txt",
                "tests/us4_1.txt",
                "tests/us5.txt",
                "tests/us5_1.txt",
                "tests/us6.txt",
                "tests/us6_1.txt",
                "tests/us7.txt",
                "tests/us8.txt"
//        EasyAccept.main(new String[]{facade, "tests/us9.txt"});
//        EasyAccept.main(new String[]{facade, "tests/us9_1.txt"});
//        EasyAccept.main(new String[]{facade, "tests/us10.txt"});
//        EasyAccept.main(new String[]{facade, "tests/us10_1.txt"
        });
    }
}