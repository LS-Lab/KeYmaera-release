package de.uka.ilkd.key.dl.arithmetics.impl.mathematica.cache;

import java.util.ArrayList;
import com.wolfram.jlink.Expr;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnableToConvertInputException;
import static de.uka.ilkd.key.dl.arithmetics.impl.mathematica.ExprConstants.*;

public class ExprRenamer {

    private static Expr[] stdExpr = { LIST, FORALL, EXISTS, INEQUALITY, LESS,
            LESS_EQUALS, GREATER_EQUALS, GREATER, PLUS, MINUS, MINUSSIGN, MULT,
            DIV, EXP, INVERSE_FUNCTION, INTEGRATE, EQUALS, UNEQUAL, AND, OR,
            NOT, IMPL, TRUE, FALSE };

    /**
     * Calculates the RenameTable-instance for the given Expression.
     * 
     * @param expr
     *            Expression to rename
     * @return RenameTable-instance
     */
    public static RenameTable getRenaming(Expr expr) {
        RenameTable newTable = new RenameTable();
        getRenamingImpl(expr, newTable);
        return newTable;
    }

    private static void getRenamingImpl(Expr expr, RenameTable table) {
        for (int i = 0; i < expr.args().length; i++) {
            getRenamingImpl(expr.args()[i], table);
        }
        
        Expr head = expr.head();
        for (int i = 0; i < stdExpr.length; i++) {
            if (head.equals(stdExpr[i]))
                return;
        }

        if (head.args().length > 0) {

            String name = expr.toString();
            if (!table.containsKey(name)) {
                int i = 0;
                while (table.containsValue("f" + i)) {
                    i++;
                }
                
                String newName = "f" + i;
                table.put(name, newName);
            }

            return;
        }


        String name = expr.toString();
        if (!table.containsKey(name)) {
            int i = 0;
            while (table.containsValue("x" + i)) {
                i++;
            }
            
            String newName = "x" + i;
            table.put(name, newName);
        }

        return;
    }

    /**
     * Renames the variables with the given renametable
     * 
     * @param expr
     *            Expression to rename
     * @param table
     *            Table with renameinformation
     * @return Renamed expression (copy)
     */
    public static Expr rename(Expr expr, RenameTable table) {
        try {
            Expr copy = renameImpl(expr, table);
            return copy;
        } catch (UnableToConvertInputException ex) {
            return expr;
        }
    }

    private static Expr renameImpl(Expr expr, RenameTable table)
            throws UnableToConvertInputException {

        // TODO: Changed-Flag einbauen und lokal auswerten, damit nicht
        // unnötig Speicher verwendet wird

        ArrayList<Expr> renamedList = new ArrayList<Expr>();
        for (int i = 0; i < expr.args().length; i++) {
            renamedList.add(renameImpl(expr.args()[i], table));
        }
        Expr[] args = renamedList.toArray(new Expr[0]);

        Expr head = expr.head();
        for (int i = 0; i < stdExpr.length; i++) {
            if (head.equals(stdExpr[i]))
                return new Expr(head, args);
        }
     
        if( expr.rationalQ() || expr.numberQ() ) {
            return new Expr( head, args);
        }
        
        String name = expr.toString();
        String newName;
        if (table.containsKey(name)) {
            newName = table.get(name);
        } else {
            newName = name;
        }
 
        if( head.args().length == 0 ) { // Variable
            return new Expr(Expr.SYMBOL, newName);
        } else { // Function
            return new Expr(new Expr(Expr.SYMBOL, newName), args); 
        }
    }
}
