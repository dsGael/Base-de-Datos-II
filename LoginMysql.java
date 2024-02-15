import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Scanner;


public class LoginMysql{
    public static void main(String[] args) throws NoSuchAlgorithmException {
      
        String URL = "jdbc:mysql://148.225.60.126/disneyplus?useSSL=false&useTimezone=true&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String db_user = "disney";
         // 148.225.60.126/phpmyadmin
        String db_password = "Ma58toAa!YLtT9S9";
        String prg_user = "gael@gmail.com";
        String prg_pwd  = "55555";
        //QUERY PARA EL ROL DE USER
        // SELECT * FROM `menu_Gael`,`user_Gael` WHERE user_Gael.email='humberto@gmail.com' AND level IN (10,20) AND menu_Gael.user_role=user_Gael.role;

        Connection conexion= getConnection(URL, db_user, db_password, prg_user, prg_pwd);
        if (conexion!=null){
            System.out.println("Bienvenido : "+prg_user);
            mainCycle(conexion,prg_user);

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
        ArrayList<Menu> menu_list = getOptionMenu(cnx, user);
        Menu parent=menu_list.get(0);
        for (Menu menu: menu_list){
            if(menu.level%10==0){
                parent=menu;
            }else{
                parent.addSubMenu(parent, menu);
            }
        }
        String option = "0";
        do{
            showMenu(menu_list);
            option=getMenu(menu_list);
            System.out.println(option);
        }while(!option.equals("0"));
    }

    public static String getMenu(ArrayList <Menu> menuList){
        Scanner scan = new Scanner(System.in);
        System.out.println(" 0: Salir");
        System.out.println("Seleccione una opción: ");
        String option=scan.nextLine();
        if (!option.equals("0")){
            int idx=Integer.parseInt(option);
            option=menuList.get(idx).menu;
        }
        return option;
    }

    public static void showMenu(ArrayList<Menu> menu_list){
        int i =0;
        for (Menu menu : menu_list) {
            if(menu.level%10==0){
                System.out.println("** "+menu.menu_text+" **");
               // showMenu(menu.subMenu);
            } else {
                
                System.out.println(" "+i+":"+menu.menu_text);
            }
            i++;
            

        }
    }

    
    public static ArrayList<Menu> getOptionMenu(Connection cnx, String role){
        ArrayList<Menu> menu_list = new ArrayList<Menu>();
       
        String query_user = "SELECT * FROM `menu_Gael`,`user_Gael` WHERE user_Gael.email='humberto@gmail.com' AND menu_Gael.user_role = user_Gael.role;";
        String query_admin = "SELECT * FROM `menu_Gael`,`user_Gael` WHERE user_Gael.email='gael@gmail.com' AND menu_Gael.user_role = user_Gael.role;";
        String query="";
        query=query_user;
        try{
            PreparedStatement ps= cnx.prepareStatement(query_admin);
            
            ResultSet rs= ps.executeQuery();
            while (rs.next()){
                String tipo = rs.getString(1);
                String menu = rs.getString(2);
                int level= (int) rs.getInt(3);
                String menu_text = rs.getString(4);
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
            String SQL="SELECT username,role FROM user_Gael WHERE email=? AND password=?";
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
    ArrayList<Menu> subMenu;

    public Menu(int level, String type, String menu, String menu_text){
        this.level = level;
        this.type = type; // User or Admin
        this.menu = menu;
        this.menu_text = menu_text; // Display text
        this.subMenu=null;
        String query_user = "SELECT * FROM `menu_Gael` WHERE level IN (10,20) AND menu_Gael.user_role = 'USER';";
        String query_admin = "SELECT * FROM `menu_Gael` WHERE level IN (10,20,30) AND menu_Gael.user_role = 'ADMIN';";
        if (type.equals("USER")){
            this.query = query_user;
        } else {
            this.query = query_admin; 

        }
    }

    public void addSubMenu(Menu parent , Menu child){
        if(parent!=null){
            if(parent.subMenu!=null){
                parent.subMenu.add(child);
            }else{
                parent.subMenu= new ArrayList<>();
                parent.subMenu.add(child);
            }
        }


    }
}
