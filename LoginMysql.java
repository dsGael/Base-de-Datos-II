import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;


public class LoginMysql{
    public static void main(String[] args) throws NoSuchAlgorithmException {
      
        String URL = "jdbc:mysql://148.225.60.126/disneyplus?useSSL=false&useTimezone=true&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String db_user = "disney";
         // 148.225.60.126/phpmyadmin
        String db_password = "Ma58toAa!YLtT9S9";
        String prg_user = "gaelborchardt@gmail.com";
        String prg_pwd  = "55555";
       

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
            System.out.println("main():"+e.getMessage());
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
            executeMenuOption(cnx, option, user);
        }while(!option.equals("0"));
    }

    public static void executeMenuOption(Connection cnx, String option, String user){
        String SQL="";
        switch (option) {
            case "LIST_USERS":
                SQL="SELECT username,email,role FROM user_Gael";
                listRecords(SQL,cnx);
                break;
            case "ADD_RATING":
                addRating(cnx,user);
                break;
            case "MOD_RATING":
                modRating(cnx,user);
                break;
            
            case "LIST_SHOWS":
                SQL="SELECT * FROM shows LIMIT 0,15";
                listRecords(SQL,cnx);
                break;
            case "DEL_RATING":
                deleteRating(cnx, user);
                break;
            case "TOP_RATINGS":
                SQL="SELECT * FROM `vista_ratings_todos` LIMIT 0,10 ";
                listRecords(SQL,cnx);
                break;
            case "TOPTEN_RATING":
                SQL="SELECT * FROM `vista_gael_ratings` LIMIT 0,10";
                listRecords(SQL,cnx);
                break;
            case "SHOW_LOG":
                SQL="SELECT * FROM `zlog_Gael` ORDER BY id DESC LIMIT 0,10 ";
                listRecords(SQL, cnx);

                break;
            case "DELETE_USER":
                break;
            case "UPDATE_USER":
                break;
            
            default:
                break;
        }

    }

    private static void modRating(Connection cnx, String user) {
       String SQL="SELECT ratings_Gael.id, ratings_Gael.id_show, shows.title, ratings_Gael.rating, ratings_Gael.timestamp FROM ratings_Gael, shows,users WHERE users.email=? AND users.id=ratings_Gael.id_user AND ratings_Gael.id_show=shows.show_id ORDER BY timestamp DESC LIMIT 0,10 ";
       Scanner scan= new Scanner(System.in); 
       try {
            PreparedStatement ps = cnx.prepareStatement(SQL);
            ps.setString(1, user);
            SQL=ps.toString();
            SQL=SQL.substring(SQL.indexOf(":")+1);
            System.out.println();
            listRecords(SQL, cnx);

            System.out.println("Ingrese el id del rating a modificar: ");
            String rating_id=scan.nextLine();
            System.out.println("Ingrese el nuevo rating: ");
            int rating_nuevo=scan.nextInt();
            SQL="UPDATE `ratings_Gael` SET `rating` = ? WHERE `ratings_Gael`.`id` = ?;";
            ps=cnx.prepareStatement(SQL);
            ps.setInt(1, rating_nuevo);
            ps.setString(2, rating_id);
            int r= ps.executeUpdate();
            System.out.println("Updated Records:"+r);
            
        }catch (SQLException e) {
            System.out.println("modRating():"+e.getMessage());
        }
    }

     private static void deleteRating(Connection cnx, String user) {
       String SQL="SELECT ratings_Gael.id, ratings_Gael.id_show, shows.title, ratings_Gael.rating, ratings_Gael.timestamp FROM ratings_Gael, shows,users WHERE users.email=? AND users.id=ratings_Gael.id_user AND ratings_Gael.id_show=shows.show_id ORDER BY timestamp DESC LIMIT 0,10 ";
       Scanner scan= new Scanner(System.in); 
       try {
            PreparedStatement ps = cnx.prepareStatement(SQL);
            ps.setString(1, user);
            SQL=ps.toString();
            SQL=SQL.substring(SQL.indexOf(":")+1);
            System.out.println(SQL);
            listRecords(SQL, cnx);

            System.out.println("¿Que registro desea borrar? ");
            String registro=scan.nextLine();
            cnx.setAutoCommit(false);
            SQL="DELETE FROM `ratings_Gael` WHERE `ratings_Gael`.`id` = ?;";

            ps=cnx.prepareStatement(SQL);
            ps.setString(1, registro);
            int r= ps.executeUpdate();
            System.out.println("Deleted Records:"+r);
            
            
            cnx.commit();

            SQL="SELECT id FROM users WHERE email=?";
            ps=cnx.prepareStatement(SQL);
            ps.setString(1, user);
            ResultSet rs=ps.executeQuery();
            int id=0;
            String name="";
            while (rs.next()) {
                id=rs.getInt(1);
            }
            SQL = "UPDATE users a INNER JOIN (SELECT COUNT(*) AS num_reviews FROM vista_ratings_Gael WHERE name=? GROUP BY name) b SET a.reviews = b.num_reviews WHERE a.name = ?";
            ps = cnx.prepareStatement(SQL);
            ps.setString(1, name);
            ps.setString(2, name);
            System.out.println(ps.toString());
            r = ps.executeUpdate();
            System.out.println("Updated "+r+" records...");
            cnx.commit();

        }catch (SQLException e) {
            System.out.println("delRating():"+e.getMessage());
        }
    }



    public static void addRating(Connection cnx, String user){
        Scanner scan= new Scanner(System.in);
        System.out.println("Ingrese el show: ");
        String show= scan.nextLine();
        String SQL="SELECT show_id,title FROM shows WHERE title LIKE ? ORDER BY title";
        show="%"+show+"%";
        try {
            PreparedStatement ps = cnx.prepareStatement(SQL);
            ps.setString(1, show);
            SQL=ps.toString();
            SQL=SQL.substring(SQL.indexOf(":")+1);
            System.out.println();
            listRecords(SQL, cnx);

            SQL="SELECT id FROM users WHERE email=?";
            ps=cnx.prepareStatement(SQL);
            ps.setString(1, user);
            ResultSet rs=ps.executeQuery();
            int id=0;
            String name="";
            while (rs.next()) {
                id=rs.getInt(1);
            }
            System.out.println("Ingrese el show_id: ");
            String show_id=scan.nextLine();
            System.out.println("Ingrese el comentario: ");
            String comentario=scan.nextLine();
            System.out.println("Ingrese el rating: ");
            int rating=scan.nextInt();
            
            SQL="INSERT INTO `ratings_Gael` (`id_user`, `id_show`, `rating`, `comments` ) VALUES ( ?, ?, ?, ?)";
            ps=cnx.prepareStatement(SQL);
            
            ps.setInt(1, id);
            ps.setString(2, show_id);
            ps.setInt(3, rating);
            ps.setString(4, comentario);
            int r= ps.executeUpdate();
            System.out.println("Inserted Records:"+r);
            cnx.setAutoCommit(false);
            SQL = "UPDATE users a INNER JOIN (SELECT COUNT(*) AS num_reviews FROM vista_ratings_Gael WHERE name=? GROUP BY name) b SET a.reviews = b.num_reviews WHERE a.name = ?";
            ps = cnx.prepareStatement(SQL);
            ps.setString(1, name);
            ps.setString(2, name);
            System.out.println(ps.toString());
            r = ps.executeUpdate();
            System.out.println("Updated "+r+" records...");
            cnx.commit();

        } catch (SQLException e) {
           System.out.println("addRating():"+e.getMessage());
        }
    }
    

    public static void displayRecords(ResultSet rs){
        try{
            
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            
            for (int i = 1; i <= columnsNumber; i++) {
                System.out.printf("%-30s",rsmd.getColumnName(i).toUpperCase());
                
            }
            while (rs.next()) {
                System.out.println();
                for (int i = 1; i <= columnsNumber; i++) {
                    System.out.printf("%-30s",rs.getString(i));
                    
                }
                
               
            }System.out.println("\n");
        }catch(Exception ex){
            System.out.println("displayRecords():"+ex.getMessage());
        }
           
    }

    private static void listRecords(String SQL, Connection cnx) {
        
        try{
            PreparedStatement ps= cnx.prepareStatement(SQL);
            ResultSet rs= ps.executeQuery();
            displayRecords(rs);
            
        }catch(Exception ex){
            System.out.println("listUsers():"+ex.getMessage());
        }
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
              
            } else {
                
                System.out.println(" "+i+":"+menu.menu_text);
            }
            i++;
            

        }
    }

    
    public static ArrayList<Menu> getOptionMenu(Connection cnx, String user){
        ArrayList<Menu> menu_list = new ArrayList<Menu>();
       
        String query_user = "SELECT * FROM `menu_Gael`,`user_Gael` WHERE user_Gael.email=? AND menu_Gael.user_role = user_Gael.role;";
        String query_admin = "SELECT * FROM `menu_Gael`,`user_Gael` WHERE user_Gael.email=? AND menu_Gael.user_role = user_Gael.role;";
        String query="";
        query=query_admin;
        
        try{
           
            PreparedStatement ps= cnx.prepareStatement(query);
            ps.setString(1, user);
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
