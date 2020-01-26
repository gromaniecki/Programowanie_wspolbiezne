import java.util.Random;
import java.util.concurrent.Semaphore;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
//---------------------------------------------------------------------------------------
/* *******************************************************************************************
            ALGORYTM ROZWIĄZUJĄCY PROBLEM CZYTELNIKÓW I PISARZY - WERSJA Z TIMEREM
                                        AHE W ŚWIDNICY
                                        WYKONALI:
                                        GRZEGORZ ROMANIECKI, MAREK KUBIAK
**********************************************************************************************
 */
class Problem {

    static int licznikczytelnikow = 0;
    static int pisarzwkolejce =0;
    static int czytelnikwkolejce=0;
    static boolean zatrzymajprogram = false;
    static Semaphore x = new Semaphore(1);          //Sekcja krytyczna
    // Tylko jeden wątek czytelnika może w niej być
    static Semaphore czytac = new Semaphore(80);    //Maksymalna ilość czytelników w czytelni
    static Semaphore pisac = new Semaphore(1);      //Semafor pisarza

    static class Czytelnik implements Runnable {
        int czasczytaniamin, czasczytaniamax, czasprzerwymin, czasprzerwymax;
        //----------------------------------------------------------------------
        public Czytelnik(int t_c_min, int t_c_max, int t_c_p_min, int t_c_p_max){
            czasczytaniamin = t_c_min;
            czasczytaniamax = t_c_max;
            czasprzerwymin = t_c_p_min;
            czasprzerwymax = t_c_p_max;
        }
        //--------------------------------------------------------------------------------------------
        public void run() {
            for (;;){
                Random przerwa = new Random();
                int przer = przerwa.nextInt(czasprzerwymax-czasprzerwymin+1)+czasprzerwymin;//generator

                if (pisarzwkolejce==0){
                    try {
                        Thread.sleep(przer);
                        czytac.acquire();  //Wejście do kolejki do czytelni
                        czytelnikwkolejce++;
                        x.acquire();  // Krytyczna wejscie
                        czytelnikwkolejce--;
                        licznikczytelnikow++;
                        if (licznikczytelnikow == 1)
                            pisac.acquire();  //zablokuj wstęp pisarzowi
                        x.release();   //Krytyczna wyjście
                        System.out.println("Liczba czytelników | "+licznikczytelnikow+"  " +
                                "||Pisarzy w kolejce "+pisarzwkolejce);
                        Random rand = new Random();
                        int a = rand.nextInt(czasczytaniamax-czasczytaniamin+1)+czasczytaniamin;//generator
                        Thread.sleep(a);  //losowy czas od 0 do X sekund
                        x.acquire();  //Krytyczna wejscie
                        licznikczytelnikow--;
                        System.out.println("Liczba czytelników | "+licznikczytelnikow+"  " +
                                "||Pisarzy w kolejce "+pisarzwkolejce);
                        if (licznikczytelnikow == 0)
                            pisac.release();
                        x.release();   //Krytyczna wyjscie
                        czytac.release();   //Zwalniam miejsce w czytelni
                        if (zatrzymajprogram)
                            break;
                    }
                    catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
    }

    static class Pisarz implements Runnable {
        int czaspisaniamin, czaspisaniamax, czasprzerwymin, czasprzerwymax;
        //------------------------------------------------------------------------
        public Pisarz(int t_p_min, int t_p_max, int t_p_p_min, int t_p_p_max){
            czaspisaniamin = t_p_min;
            czaspisaniamax = t_p_max;
            czasprzerwymin = t_p_p_min;
            czasprzerwymax = t_p_p_max;
        }
        //--------------------------------------------------------------------------
        public void run() {
            for (;;){
                Random przerwa = new Random();
                int przer = przerwa.nextInt(czasprzerwymax-czasprzerwymin+1)+czasprzerwymin;//generator
                try {
                    int a=(przer);  //losowy czas od 0 do X sekund
                    Thread.sleep(a);
                    czytac.acquire();  //Zamknij wejście do czytelni
                    pisarzwkolejce++;
                    pisac.acquire();    //Zaczynam pisanie
                    pisarzwkolejce--;
                    System.out.print(Thread.currentThread().getName() + " wchodzi -->");
                    Random rand = new Random();
                    int b = rand.nextInt(czaspisaniamax-czaspisaniamin+1)+czaspisaniamin;//generator
                    Thread.sleep(b);  //losowy czas od min do max
                    System.out.println(" ...i wychodzi po --> "+(b)+" ms || W kolejce jest w tej chwili "+czytelnikwkolejce+" czytelników.");
                    pisac.release();   //Koncze pisanie
                    czytac.release(); //Otwórz wejście do czytelni
                    if (zatrzymajprogram)
                        break;
                }
                catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
    static class licznik implements Runnable{
        int stop;
        public licznik (int t_stop){
            stop = t_stop*1000;
        }
        public void run (){
            try{
                Thread.sleep(stop);
                zatrzymajprogram = true;
            }
            catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int[] tab = new int[11];
        try {
            // Uworzenie obiektu FileReader
            FileReader fileReader = new FileReader("src//dane.txt");
            // Utworzenie obiektu bufferReader
            BufferedReader bufferReader = new BufferedReader(fileReader);
            String linia;
            int i =0;
            //-----------------------------------------------------------------------------------
            while((linia = bufferReader.readLine()) != null) {
                tab [i] = Integer.parseInt(linia);
                i++;
            }
            fileReader.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        //-------------------------------------------------------------------------
        int C = tab[0];         //liczba czytelników
        int P = tab[1];         //liczba pisarzy
        int t_c_p_max = tab[2]; //maksymalny czas przerwy czytelnika
        int t_c_p_min = tab[3]; //minimalny czas przerwy czytelnika
        int t_c_min = tab[4];   //minimalny czas czytania ms
        int t_c_max = tab[5];   //maksymalny czas czytania ms
        int t_p_min = tab[6];   //minimalny czas pisania ms
        int t_p_max = tab[7];   //maksymalny czas pisania ms
        int t_p_p_min = tab[8]; //minimalna przerwa pisarza
        int t_p_p_max = tab[9]; //maksymalna przerwa pisarza
        int t_stop = tab[10];   //czas działania programu
        //----------------------------------------------------------------------------
        Czytelnik read = new Czytelnik(t_c_min , t_c_max , t_c_p_min , t_c_p_max);
        Pisarz write = new Pisarz(t_p_min , t_p_max , t_p_p_min , t_p_p_max);
        licznik stop = new licznik(t_stop);
        Thread zatrzymaj = new Thread(stop);
        zatrzymaj.start();
        for(;;) {
            if (C>0){
                Thread t1 = new Thread(read);
                t1.start();
                C--;
            }
            if (P>0){
                Thread t2 = new Thread(write);
                t2.setName("Pisarz "+P);
                t2.start();
                P--;
            }
            if (C==0 && P == 0)
                break;
        }
    }
}