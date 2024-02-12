import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.sql.DriverManager;


public class LoginMysql{
    public static void main(String[] args) throws NoSuchAlgorithmException {
      
        String URL = "jdbc:mysql://148.225.60.126/disney?useSSL=false&useTimezone=true&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String db_user = "disney";
         // 148.225.60.126/phpmyadmin
        String db_password = "Ma58toAa!YLtT9S9";
        String prg_user = "gael@gmail.com";
        String prg_pwd  = "55555";

        Connection conexion= getConnection(URL, db_user, db_password, prg_user, prg_pwd);
        if (conexion!=null){
            System.out.println("Bienvenido : "+prg_user);
            mainCycle(conexion,prg_user)

        }else{
            System.out.println("Acceso Denegado");
        }
        try {
            conexion.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
    
    }

    public static void mainCycle(Connection cnx, String user){

    }

    
    public static ArrayList<Menu> getOptionMenu(Connection cnx, String role){
        ArrayList<Menu> menu_list = new ArrayList<Menu>();
       
        String query_user = "SELECT * FROM `menu_Gael` WHERE level IN (10,20) AND menu_Gael.user_role = 'USER';";
        String query_admin = "SELECT * FROM `menu_Gael` WHERE level IN (10,20,30) AND menu_Gael.user_role = 'ADMIN';";
        String query="";
        if(role.equals("USER")){
            query=query_user;
        }else{  
            query=query_admin;
        }
        try{
            PreparedStatement ps= cnx.prepareStatement(query_user);
            ResultSet rs= ps.executeQuery();
            while (rs.next()){
                String tipo = rs.getString(0);
                String menu = rs.getString(1);
                int level= (int) rs.getInt(2);
                String menu_text = rs.getString(3);
                Menu menu_row=(new Menu(level, tipo, menu, menu_text));
                menu_list.add(menu_row);
            }
        }catch(Exception ex){
            System.out.println("getOptionMenu():"+ex.getMessage());
        }
        return menu_list;
    }

   
    public static byte[] getSHA(String input) throws NoSuchAlgorithmException {
        byte[] hash = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            System.out.println("getSHA: "+ex.getMessage());
        }
        return hash;
    }
    
    public static String hexString(byte[] hash) {
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length()< 32){
            hexString.insert(0,'0');
        }
        return hexString.toString();
    }
    
    public static Connection getConnection(String URL, String db_user, String db_password, String prg_user, String pgr_password){
        Connection cnx=null;
        try{
            cnx= DriverManager.getConnection(URL, db_user, db_password);
            System.out.println("Succesful Connection");
            String SQL="SELECT username FROM users_Gael WHERE email=? AND password=?";
            PreparedStatement ps=cnx.prepareStatement(SQL);
            ps.setString(1, prg_user);
            ps.setString(2, pgr_password);
            ResultSet rs= ps.executeQuery();
            

        }catch(Exception ex){
            System.out.println("getConnection():"+ex.getMessage());
        }
        return cnx;
    }

}

class Menu {
    int level;
    String type;
    String menu;
    String menu_text;
    String query;

    public Menu(int level, String type, String menu, String menu_text){
        this.level = level;
        this.type = type; // User or Admin
        this.menu = menu;
        this.menu_text = menu_text; // Display text
        String query_user = "SELECT * FROM `menu_Gael` WHERE level IN (10,20) AND menu_Gael.user_role = 'user';";
        String query_admin = "SELECT * FROM `menu_Gael` WHERE level IN (10,20,30) AND menu_Gael.user_role = 'admin';";
        if (type.equals("user")){
            this.query = query_user;
        } else {
            this.query = query_admin; 

        }
    }
}