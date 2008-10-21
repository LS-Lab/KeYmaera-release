package de.uka.ilkd.key.dl.arithmetics.impl.mathematica.cache;

import java.util.ArrayList;
import com.wolfram.jlink.Expr;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnableToConvertInputException;
import static de.uka.ilkd.key.dl.arithmetics.impl.mathematica.ExprConstants.*;

public class ExprRenamer {
    
    // TODO: "E"
    // TODO: symbolQ
    // TODO: numberQ
    private static Expr[] stdExpr = {
        LIST,
        FORALL,
        EXISTS,
        INEQUALITY,
        LESS,
        LESS_EQUALS,
        GREATER_EQUALS,
        GREATER,
        PLUS,
        MINUS,
        MINUSSIGN,
        MULT,
        DIV,
        EXP,
        INVERSE_FUNCTION,
        INTEGRATE,
        EQUALS,
        UNEQUAL,
        AND,
        OR,
        NOT,
        IMPL,
        TRUE,
        FALSE
    };

    public ExprRenamer() {

    }

    public RenameTable getRenaming(Expr expr) {
        return null;
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
    public Expr rename(Expr expr, RenameTable table) {
        try { 
            Expr copy = renameImpl(expr, table);
            return copy;
        } catch( UnableToConvertInputException ex ) {
            return expr;
        }
    }

    private Expr renameImpl(Expr expr, RenameTable table) throws UnableToConvertInputException {
        
        // TODO: Changed-Flag einbauen und lokal auswerten, damit nicht 
        // unnötig Speicher verwendet wird
        
        // TODO: Zahlen, wie werden sie behandelt, falls es nicht schon geschehen ist?
        
        // Argumente auch umbenennen (mittels Rekursion)
        ArrayList<Expr> renamedList = new ArrayList<Expr>();
        for( int i = 0; i < expr.args().length ; i++ ) {
            renamedList.add( renameImpl( expr.args()[i], table ));
        }
        Expr[] args = renamedList.toArray(new Expr[0]);
        
        // Operatoren und andere
        Expr head = expr.head();     
        for( int i = 0; i < stdExpr.length; i++ ) {
            if( head.equals(stdExpr[i]))
                return new Expr(head,args);
        }
        
        // Funktion
        if( head.args().length > 0 ) {
            System.out.println("Funktion erkannt!");
            
            // Funktionsnamen umbenennen
            String name = expr.toString();
            String newName = "";
            if( table.containsKey(name)) {
                newName = table.get(name);
            } else {
                newName = name;
            }
            
            System.out.println("Funktion " + name + " in " + newName + " unbenannt");
            
            // TODO: nachschauen, ob es richtig ist...
            return new Expr( new Expr(Expr.SYMBOL, newName), args );
        }
        
        // Sonstiges --> Variablen
        // hier umbenennen!
        String name = expr.toString();
        String newName;
        if( table.containsKey(name)) {
            newName = table.get(name);
        } else {
            newName = name;
        }
        System.out.println("Variable " + name + " in " + newName + " umbenannt");
        
        return new Expr(Expr.SYMBOL, newName );

        
//        try {
//            if (expr.toString().equalsIgnoreCase("$Aborted")
//                    || expr.toString().equalsIgnoreCase("Abort[]")) {
//                throw new IncompleteEvaluationException("Calculation aborted!");
//            } else if (expr.toString().equalsIgnoreCase("$Failed")) {
//                throw new FailedComputationException("Calculation failed!");
//                
//            // Quantoren
//            } else if (expr.head().equals(FORALL) || expr.head().equals(EXISTS)) {
//                Expr list = expr.args()[0];
//                if (list.head().equals(LIST)) { // LIST
// 
//                    List<LogicVariable> vars = new ArrayList<LogicVariable>();
//                    for (int i = 0; i < list.args().length; i++) {
//                        Name name;
//                        String asString = list.args()[i].asString();
//                        if (asString.endsWith("$")) {
//                            name = new Name(asString.substring(0, asString
//                                    .length() - 1));
//                        } else {
//                            name = new Name(asString);
//                        }
//                        name = new Name(name.toString().replaceAll(
//                                USCORE_ESCAPE, "_"));
//                        LogicVariable lookup = (LogicVariable) nss.variables()
//                                .lookup(name);
//                        if (lookup == null) {
//                            lookup = new LogicVariable(name, RealLDT
//                                    .getRealSort());
//
//                        }
//                        vars.add(lookup);
//                        quantifiedVariables.put(name, lookup);
//                    }
//                    if (expr.head().equals(EXISTS)) {
//                        Term result = convertImpl(expr.args()[1], nss,
//                                quantifiedVariables);
//                        for (LogicVariable var : vars) {
//                            result = TermBuilder.DF.ex(var, result);
//                        }
//                        return result;
//                    } else {
//                        return TermBuilder.DF.all(vars
//                                .toArray(new LogicVariable[0]), convertImpl(
//                                expr.args()[1], nss, quantifiedVariables));
//                    }
//                }
//            }
//            if (expr.head().equals(INEQUALITY)) {
//                Term result = null;
//                Term left = convert(expr.args()[0], nss, quantifiedVariables);
//                Term mid = convert(expr.args()[2], nss, quantifiedVariables);
//                Term right = convert(expr.args()[4], nss, quantifiedVariables);
//                String func1 = "";
//                String func2 = "";
//                if (expr.args()[1].equals(LESS)) {
//                    func1 = "lt";
//                } else if (expr.args()[1].equals(LESS_EQUALS)) {
//                    func1 = "leq";
//                } else if (expr.args()[1].equals(GREATER_EQUALS)) {
//                    func1 = "geq";
//                } else if (expr.args()[1].equals(GREATER)) {
//                    func1 = "gt";
//                }
//                if (expr.args()[3].equals(LESS)) {
//                    func2 = "lt";
//                } else if (expr.args()[3].equals(LESS_EQUALS)) {
//                    func2 = "leq";
//                } else if (expr.args()[3].equals(GREATER_EQUALS)) {
//                    func2 = "geq";
//                } else if (expr.args()[3].equals(GREATER)) {
//                    func2 = "gt";
//                }
//                result = TermBuilder.DF.func(lookupRigidFunction(nss, new Name(
//                        func1), 1), left, mid);
//                result = TermBuilder.DF.and(result, TermBuilder.DF.func(
//                        lookupRigidFunction(nss, new Name(func2), 1), mid,
//                        right));
//                return result;
//            }
//            Term[] ex = new Term[expr.args().length];
//            for (int i = 0; i < ex.length; i++) {
//                ex[i] = convert(expr.args()[i], nss, quantifiedVariables);
//                if (ex[i] == null) {
//                    throw new UnableToConvertInputException("Fail",
//                            new UnableToConvertInputException("Converting "
//                                    + expr.args()[i] + " failed!"));
//                }
//            }
//            if (expr.rationalQ()) {
//                return TermBuilder.DF.func(lookupRigidFunction(nss, new Name(
//                        "div"), 2), ex[0], ex[1]);
//            } else if (expr.numberQ()) {
//                BigDecimal asBigDecimal = expr.asBigDecimal();
//                boolean minus = false;
//                if (asBigDecimal.compareTo(BigDecimal.ZERO) < 0) {
//                    asBigDecimal = asBigDecimal.abs();
//                    minus = true;
//                }
//                Name name = new Name("" + asBigDecimal);
//                Function num = lookupRigidFunction(nss, name, 0);
//                Term result = TermBuilder.DF.func(num);
//                if (minus) {
//                    result = TermBuilder.DF.func(lookupRigidFunction(nss,
//                            new Name("neg"), 2), result);
//                }
//                return result;
//            } else if (expr.head().symbolQ()) {
//                if (expr.head().equals(PLUS)) {
//                    Term result = ex[0];
//                    for (int i = 1; i < ex.length; i++) {
//                        result = TermBuilder.DF.func(RealLDT
//                                .getFunctionFor(Plus.class), result, ex[i]);
//                    }
//                    return result;
//                } else if (expr.head().equals(MINUS)) {
//                    Term result = ex[0];
//                    for (int i = 1; i < ex.length; i++) {
//                        result = TermBuilder.DF
//                                .func(
//                                        RealLDT
//                                                .getFunctionFor(de.uka.ilkd.key.dl.model.Minus.class),
//                                        result, ex[i]);
//                    }
//                    return result;
//                } else if (expr.head().equals(MINUSSIGN)) {
//                    return TermBuilder.DF.func(RealLDT
//                            .getFunctionFor(MinusSign.class), ex[0]);
//                } else if (expr.head().equals(MULT)) {
//                    Term result = ex[0];
//                    for (int i = 1; i < ex.length; i++) {
//                        result = TermBuilder.DF.func(RealLDT
//                                .getFunctionFor(Mult.class), result, ex[i]);
//                    }
//                    return result;
//                } else if (expr.head().equals(DIV)) {
//                    Term result = ex[0];
//                    for (int i = 1; i < ex.length; i++) {
//                        result = TermBuilder.DF.func(RealLDT
//                                .getFunctionFor(Div.class), result, ex[i]);
//                    }
//                    return result;
//                } else if (expr.head().equals(EXP)) {
//                    Term result = ex[0];
//                    for (int i = 1; i < ex.length; i++) {
//                        result = TermBuilder.DF.func(RealLDT
//                                .getFunctionFor(Exp.class), result, ex[i]);
//                    }
//                    return result;
//                } else if (expr.head().equals(INVERSE_FUNCTION)) {
//                    Term result = ex[0];
//                    result = TermBuilder.DF.func(lookupRigidFunction(nss,
//                            new Name("InverseFunction"), 1), result, ex[1]);
//                    return result;
//                } else if (expr.head().equals(INTEGRATE)) {
//                    Term result = ex[0];
//                    result = TermBuilder.DF.func(lookupRigidFunction(nss,
//                            new Name("Integrate"), 1), result, ex[1]);
//                    return result;
//                } else if (expr.head().equals(EQUALS)) {
//                    Term result = ex[0];
//                    result = TermBuilder.DF.equals(result, ex[1]);
//                    return result;
//                } else if (expr.head().equals(LESS)) {
//                    Term result = ex[0];
//                    result = TermBuilder.DF.func(RealLDT
//                            .getFunctionFor(Less.class), result, ex[1]);
//                    return result;
//                } else if (expr.head().equals(GREATER)) {
//                    Term result = ex[0];
//                    result = TermBuilder.DF
//                            .func(
//                                    RealLDT
//                                            .getFunctionFor(de.uka.ilkd.key.dl.model.Greater.class),
//                                    result, ex[1]);
//                    return result;
//                } else if (expr.head().equals(GREATER_EQUALS)) {
//                    Term result = ex[0];
//                    result = TermBuilder.DF
//                            .func(RealLDT.getFunctionFor(GreaterEquals.class),
//                                    result, ex[1]);
//                    return result;
//                } else if (expr.head().equals(LESS_EQUALS)) {
//                    Term result = ex[0];
//                    result = TermBuilder.DF.func(RealLDT
//                            .getFunctionFor(LessEquals.class), result, ex[1]);
//                    return result;
//                } else if (expr.head().equals(UNEQUAL)) {
//                    Term result = ex[0];
//                    result = TermBuilder.DF.func(RealLDT
//                            .getFunctionFor(Unequals.class), result, ex[1]);
//                    return result;
//                } else if (expr.head().equals(AND)) {
//                    Term result = ex[0];
//                    for (int i = 1; i < ex.length; i++) {
//                        result = TermBuilder.DF.and(result, ex[i]);
//                    }
//                    return result;
//                } else if (expr.head().equals(OR)) {
//                    Term result = ex[0];
//                    for (int i = 1; i < ex.length; i++) {
//                        result = TermBuilder.DF.or(result, ex[i]);
//                    }
//                    return result;
//                } else if (expr.head().equals(NOT)) {
//                    return TermBuilder.DF.not(ex[0]);
//                } else if (expr.head().equals(IMPL)) {
//                    assert ex.length == 2 : "associativity unclear except for binary";
//                    Term result = ex[0];
//                    for (int i = 1; i < ex.length; i++) {
//                        result = TermBuilder.DF.imp(result, ex[i]);
//                    }
//                    return result;
//                } else if (expr.head().equals(TRUE)) {
//                    return TermBuilder.DF.tt();
//                } else if (expr.head().equals(FALSE)) {
//                    return TermBuilder.DF.ff();
//                } else if (expr.toString().equals("E")
//                        || expr.head().toString().equals("E")) {
//                    Name e = new Name("E");
//                    Function funcE = (Function) nss.functions().lookup(e);
//                    if (funcE == null) {
//                        funcE = lookupRigidFunction(nss, e, 0);
//                    }
//                    return TermBuilder.DF.func(funcE);
//                } else if (ex.length > 0) {
//                    Name name = new Name(expr.head().asString());
//                    name = new Name(name.toString().replaceAll(USCORE_ESCAPE,
//                            "_"));
//
//                    Function f = (Function) nss.functions().lookup(name);
//                    if (f == null) {
//                        if (isBlacklisted(name)) {
//                            throw new RemoteException(
//                                    "Mathematica returned a system function "
//                                            + name);
//                        }
//                        Sort[] argSorts = new Sort[ex.length];
//                        Arrays.fill(argSorts, RealLDT.getRealSort());
//                        f = new RigidFunction(name, RealLDT.getRealSort(),
//                                argSorts, FunctionType.MATHFUNCTION);
//                        nss.functions().add(f);
//                    }
//                    if (expr.args().length > 0) {
//                        return TermBuilder.DF.func(f, ex);
//                    }
//                    return TermBuilder.DF.func(f);
//                } else {
//                    Name name;
//                    if (expr.asString().endsWith("$")) {
//                        name = new Name(expr.asString().substring(0,
//                                expr.asString().length() - 1));
//                    } else {
//                        name = new Name(expr.toString());
//                    }
//                    name = new Name(name.toString().replaceAll(USCORE_ESCAPE,
//                            "_"));
//                    if (isBlacklisted(name)) {
//                        throw new RemoteException(
//                                "Mathematica returned a system function "
//                                        + name);
//                    }
//                    if (quantifiedVariables.containsKey(name)) {
//                        return TermBuilder.DF
//                                .var(quantifiedVariables.get(name));
//                    }
//                    Named var = nss.variables().lookup(name);
//                    if (var != null) {
//                        if (var instanceof LogicVariable) {
//                            return TermBuilder.DF.var((LogicVariable) var);
//                        } else if (var instanceof Metavariable) {
//                            return TermFactory.DEFAULT
//                                    .createFunctionTerm((Metavariable) var);
//                        }
//                    } else {
//                        var = nss.functions().lookup(name);
//                        if (var != null) {
//                            return TermBuilder.DF.func((Function) var, ex);
//                        } else {
//                            var = nss.programVariables().lookup(name);
//                            if (var == null) {
//                                // var = new
//                                // de.uka.ilkd.key.logic.op.LocationVariable(
//                                // new ProgramElementName(name.toString()),
//                                // getSortR(nss));
//                                throw new UnableToConvertInputException(
//                                        "ProgramVariable " + name
//                                                + " is not declared");
//                            }
//                            return TermBuilder.DF
//                                    .var((de.uka.ilkd.key.logic.op.ProgramVariable) var);
//                        }
//                    }
//                }
//            }
//        } catch (ExprFormatException e) {
//            throw new UnableToConvertInputException("Error converting Expr "
//                    + expr + " to Formula, because " + e, e);
//        }
//        throw new UnableToConvertInputException("Dont know how to convert "
//                + expr);
    }
}
