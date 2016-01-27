/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Gui;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.Color;
import java.awt.*;
import java.util.ArrayList;

/**
 *
 * @author Taihao
 */
public class ComponentBackgroundMaker extends JLabel implements TableCellRenderer {
        int col;
        ArrayList<Color> colors;
        public ComponentBackgroundMaker(JTable table,int col,ArrayList<Color> colors){
            this.colors=colors;
            this.col=col;
            setOpaque(false);
            int rows=table.getRowCount(),i;
            table.getColumnModel().getColumn(col).setCellRenderer(this);
            setHorizontalAlignment(SwingConstants.RIGHT);
       }
       public Component getTableCellRendererComponent(
		JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
           setText(value.toString());
            if(column==col&&row<colors.size()){
                setForeground(colors.get(row));
//                setBackground(colors.get(row));
            } else
                setForeground(Color.white);
            return this;
        }
}
