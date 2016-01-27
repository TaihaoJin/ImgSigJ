/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Non_LinearFitting;
import utilities.Non_LinearFitting.ButtonColumn;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.util.ArrayList;
import utilities.CommonMethods;
import utilities.CommonStatisticsMethods;;

/**
 *
 * @author Taihao
 */
public class MultipleButtonColumn extends ButtonColumn{
        int rows,cols;
        ArrayList<Integer> columns;
        TableCellEditor originalCellEditor[][];
        TableCellRenderer originalCellRenderer[][];
	public MultipleButtonColumn(JTable table, Action action, ArrayList<Integer> columns)
	{
                this.columns=new ArrayList();
                CommonStatisticsMethods.copyArray(columns, this.columns);
		this.table = table;
		this.action = action;
                cols=table.getColumnCount();
                rows=table.getRowCount();
                preserveOriginalCellComponents();

		renderButton = new JButton();
		editButton = new JButton();
		editButton.setFocusPainted( false );
		editButton.addActionListener( this );
		originalBorder = editButton.getBorder();
		setFocusBorder( new LineBorder(Color.BLUE) );

		TableColumnModel columnModel = table.getColumnModel();
                int i,column,len=columns.size();
                len=cols;
                for(i=0;i<len;i++){
//                    column=columns.get(i);
                    column=i;
                    columnModel.getColumn(column).setCellRenderer( this );
                    columnModel.getColumn(column).setCellEditor( this );
                }
		table.addMouseListener( this );
	}
        void preserveOriginalCellComponents(){
           originalCellEditor=new  TableCellEditor[rows][cols];
            originalCellRenderer=new TableCellRenderer[rows][cols];
            int i,j;
            for(i=0;i<rows;i++){
                for(j=0;j<cols;j++){
                    originalCellEditor[i][j]=table.getCellEditor(i, j);
                    originalCellRenderer[i][j]=table.getCellRenderer(i, j);
                }
            }
        }
	public void actionPerformed(ActionEvent e)
	{
		int row = table.convertRowIndexToModel( table.getEditingRow() );
		int column = table.convertRowIndexToModel( table.getEditingColumn() );
		fireEditingStopped();

		//  Invoke the Action

		ActionEvent event = new ActionEvent(
			table,
			ActionEvent.ACTION_PERFORMED,
			"" + row+" "+column);
		action.actionPerformed(event);
	}
	public Component getTableCellEditorComponent(
		JTable table, Object value, boolean isSelected, int row, int column)
	{
                if(!CommonMethods.containsContent(columns, column)) return originalCellEditor[row][column].getTableCellEditorComponent(table, value, isSelected, row, column);
		if (value == null)
		{
			editButton.setText( "" );
			editButton.setIcon( null );
		}
		else if (value instanceof Icon)
		{
			editButton.setText( "" );
			editButton.setIcon( (Icon)value );
		}
		else
		{
			editButton.setText( value.toString() );
			editButton.setIcon( null );
		}

		this.editorValue = value;
		return editButton;
	}
	public Component getTableCellRendererComponent(
		JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
                if(!CommonMethods.containsContent(columns, column)) return originalCellRenderer[row][column].getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (isSelected)
		{
			renderButton.setForeground(table.getSelectionForeground());
	 		renderButton.setBackground(table.getSelectionBackground());
		}
		else
		{
                    if(row>7&&row<10){
			renderButton.setForeground(Color.BLACK);
			renderButton.setBackground(Color.GREEN);
                    }else{
			renderButton.setForeground(table.getForeground());
			renderButton.setBackground(UIManager.getColor("Button.background"));
                    }
		}

		if (hasFocus)
		{
			renderButton.setBorder( focusBorder );
		}
		else
		{
			renderButton.setBorder( originalBorder );
		}
		if (value == null)
		{
			renderButton.setText( "" );
			renderButton.setIcon( null );
		}
		else if (value instanceof Icon)
		{
			renderButton.setText( "" );
			renderButton.setIcon( (Icon)value );
		}
		else
		{
			renderButton.setText( value.toString() );
			renderButton.setIcon( null );
		}

		return renderButton;
        }
}
