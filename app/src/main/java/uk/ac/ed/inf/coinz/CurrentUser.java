package uk.ac.ed.inf.coinz;

public class CurrentUser {
    private static String email;
    private static int numberOfCoinsCollected;
    public void setEmail(String email){
        this.email=email;
    }
    public String getEmail(){
        return email;
    }
    public void setnumberOfCoinsCollected(int numberOfCoinsCollected){
        this.numberOfCoinsCollected=numberOfCoinsCollected;
    }
    public int getNumberOfCoinsCollected(){
        return numberOfCoinsCollected;
    }
}
