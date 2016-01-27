/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.io;
import javax.swing.*;
/**
 *
 * @author Taihao
 */
public class MessageAssist {
    public static void error(String message){
        int mc = JOptionPane.ERROR_MESSAGE;
        JOptionPane.showMessageDialog (null, message, "Errors", mc);
    }
}
