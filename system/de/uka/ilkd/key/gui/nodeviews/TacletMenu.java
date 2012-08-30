// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//
package de.uka.ilkd.key.gui.nodeviews;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.collection.ImmutableSLList;
import de.uka.ilkd.key.dl.rules.EliminateExistentialQuantifierRule;
import de.uka.ilkd.key.dl.rules.ReduceRule;
import de.uka.ilkd.key.dl.rules.ReduceRuleApp;
import de.uka.ilkd.key.dl.strategy.tactics.ApplyEquationTactic;
import de.uka.ilkd.key.dl.strategy.tactics.SkolemizeTactic;
import de.uka.ilkd.key.gui.KeYMediator;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.gui.ReduceRulesItem;
import de.uka.ilkd.key.gui.assistant.AIAction;
import de.uka.ilkd.key.gui.assistant.BuiltInRuleSelectedInput;
import de.uka.ilkd.key.gui.assistant.ProofAssistantController;
import de.uka.ilkd.key.gui.assistant.RuleEventInput;
import de.uka.ilkd.key.gui.configuration.Config;
import de.uka.ilkd.key.gui.configuration.ProofSettings;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.logic.JavaBlock;
import de.uka.ilkd.key.logic.NameCreationInfo;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.ProgramElementName;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.ProgramVariable;
import de.uka.ilkd.key.logic.op.SchemaVariable;
import de.uka.ilkd.key.logic.op.SortedSchemaVariable;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.pp.AbbrevException;
import de.uka.ilkd.key.pp.AbbrevMap;
import de.uka.ilkd.key.pp.PosInSequent;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.BuiltInRule;
import de.uka.ilkd.key.rule.FindTaclet;
import de.uka.ilkd.key.rule.RewriteTaclet;
import de.uka.ilkd.key.rule.RewriteTacletGoalTemplate;
import de.uka.ilkd.key.rule.Taclet;
import de.uka.ilkd.key.rule.TacletApp;
import de.uka.ilkd.key.rule.TacletGoalTemplate;
import de.uka.ilkd.key.rule.TacletSchemaVariableCollector;
import de.uka.ilkd.key.smt.DecProcRunner;
import de.uka.ilkd.key.smt.SMTRule;
import de.uka.ilkd.key.smt.SMTRuleMulti;
import de.uka.ilkd.key.util.Debug;

/**
 *  This class creates a menu with Taclets as entries. The invoker has
 * to be of type SequentView because of the method call selectedTaclet
 * that hands over the selected Taclet. The class is used to get all
 * Taclet that are applicable at a selected position in a sequent.
 */ 
class TacletMenu extends JMenu {

    private static final String SKOLEMIZE_NON_RIGIDS = "skolemize non-rigids";

    private static final String TO_CLIPBOARD = "to clipboard";

    private PosInSequent pos;
    private SequentView sequentView;
    private KeYMediator mediator;

    private TacletAppComparator comp = new TacletAppComparator();
    
    static Logger logger = Logger.getLogger(TacletMenu.class.getName());
    private final Goal selectedGoal;
    
    /** 
     * creates empty menu 
     */
    TacletMenu() {
	selectedGoal = null;
    }


    /** creates a new menu that displays all applicable rules at the given
     * position
     * @param sequentView the SequentView that is the parent of this menu
     * @param findList IList<Taclet> with all applicable FindTaclets
     * @param rewriteList IList<Taclet> with all applicable RewriteTaclets
     * @param noFindList IList<Taclet> with all applicable noFindTaclets
     * @param builtInList IList<BuiltInRule> with all applicable BuiltInRules
     * @param pos the PosInSequent
     */ 
    TacletMenu(SequentView sequentView,
	    	final Goal selectedGoal,
	       ImmutableList<TacletApp> findList, ImmutableList<TacletApp> rewriteList,
	       ImmutableList<TacletApp> noFindList, ImmutableList<BuiltInRule> builtInList,
	       PosInSequent pos) {
        super();        
	this.sequentView = sequentView;
	this.mediator = sequentView.mediator();
 	this.pos = pos;
 	this.selectedGoal = selectedGoal;
	// delete RewriteTaclet from findList because they will be in
	// the rewrite list and concatenate both lists
	createTacletMenu(removeNonInteractives(removeRewrites(findList).prepend(rewriteList)),
			 noFindList, builtInList, new MenuControl());
    }

    
    /**
     * @param prepend
     * @return
     */
    private ImmutableList<TacletApp> removeNonInteractives(
            ImmutableList<TacletApp> prepend) {
        if(ProofSettings.DEFAULT_SETTINGS.getViewSettings().isShowNonInteractiveRules()) {
            return prepend;
        } else {
        	ImmutableList<TacletApp> result = ImmutableSLList.<TacletApp>nil();
            for(final TacletApp ta: prepend) {
                if(!ta.taclet().noninteractive()) {
                    result = result.append(ta);
                }
            }
            return result;
        }
    }


    /** removes RewriteTaclet from list
     * @param list the IList<Taclet> from where the RewriteTaclet are
     * removed
     * @return list without RewriteTaclets
     */
    private ImmutableList<TacletApp> removeRewrites(ImmutableList<TacletApp> list) {
	ImmutableList<TacletApp> result = ImmutableSLList.<TacletApp>nil();

        for (final TacletApp tacletApp : list) {
            result = (tacletApp.taclet() instanceof RewriteTaclet ? result :
                    result.prepend(tacletApp));
        }
	return result;
    }


    /** creates the menu by adding all submenus and items */
    private void createTacletMenu(ImmutableList<TacletApp> find,
				  ImmutableList<TacletApp> noFind,
				  ImmutableList<BuiltInRule> builtInList,
				  MenuControl control) {	 
	addActionListener(control);
	boolean rulesAvailable=(addSection("Find", sort(find), control));
	if (pos != null && pos.isSequent()) {
	    rulesAvailable=addSection("NoFind", noFind, control)
		| rulesAvailable;
	}
	if (!rulesAvailable) {
	    createSection("No rules applicable.");
	}

	createBuiltInRuleMenu(builtInList, control);
        
	addSkolemizeTactic(control);
	
	addApplyEqTactics(control);
	
	createFocussedAutoModeMenu ( control );
    
	//        addPopFrameItem(control);
	addClipboardItem(control);

	if (pos != null) {
	    PosInOccurrence occ = pos.getPosInOccurrence();	    
	    if (occ != null && occ.posInTerm() != null) {
		Term t = occ.subTerm ();
		createAbbrevSection(t, control);

		if(t.op() instanceof ProgramVariable) {
		    ProgramVariable var = (ProgramVariable)t.op();
		    if(var.getProgramElementName().getCreationInfo() != null) {
		    	createNameCreationInfoSection(control);
		    }
		}
	    }
	}
    }

    private void createBuiltInRuleMenu(ImmutableList<BuiltInRule> builtInList,
				       MenuControl            control) {

	if (!builtInList.isEmpty()) {
	    addSeparator();
        for (BuiltInRule aBuiltInList : builtInList) {
            addBuiltInRuleItem(aBuiltInList, control);
        }
	}
    }
				      
    /**
     * adds an item for built in rules (e.g. Run Simplify or Update Simplifier)
     */
    private void addBuiltInRuleItem(final BuiltInRule builtInRule,
				    MenuControl control) {
        final JMenuItem item;
        if (builtInRule instanceof ReduceRule
                || builtInRule instanceof EliminateExistentialQuantifierRule) {
            item = new ReduceRulesItem(mediator.mainFrame(), builtInRule,
                    mediator.getSelectedProof(), pos.getPosInOccurrence());
        } else {
        	item = new DefaultBuiltInRuleMenuItem(builtInRule);                       
        }
        item.addActionListener(control);
        add(item);
        // add listeners to show tips in the proof assistant
        ((JMenuItem) item).addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                MenuElement[] selectedPath = MenuSelectionManager
                        .defaultManager().getSelectedPath();
                if (selectedPath.length > 0
                        && selectedPath[selectedPath.length - 1] == item) {
                    ProofAssistantController proofAssistantController = Main
                            .getInstance().getProofAssistantController();
                    AIAction analyze = proofAssistantController
                            .getAssistantAI().analyze(
                                    new BuiltInRuleSelectedInput(builtInRule.name().toString().replace(' ', '_')));
                    if (analyze != null) {
                        analyze.execute(proofAssistantController);
                    }
                }
            }
        });

    }

    
    private void createFocussedAutoModeMenu (MenuControl control) {
        addSeparator();
        JMenuItem item = new FocussedRuleApplicationMenuItem ();
        item.addActionListener(control);
        add(item);        
    }
    

    private ImmutableList<TacletApp> sort(ImmutableList<TacletApp> finds) {
	ImmutableList<TacletApp> result = ImmutableSLList.<TacletApp>nil();
	
	List<TacletApp> list = new ArrayList<TacletApp>(finds.size());

	for (final TacletApp app : finds) {
	    list.add(app);
	}

	Collections.sort(list, comp);

	for (final TacletApp app : list) {
	    result = result.prepend(app);
	}

	return result;
    }


    private void createAbbrevSection(Term t, MenuControl control){
	AbbrevMap scm = mediator.getNotationInfo().getAbbrevMap();
	JMenuItem sc  ;
	if(scm.containsTerm(t)){
	    sc = new JMenuItem("Change abbreviation");
	    sc.addActionListener(control);
	    add(sc);
	    if(scm.isEnabled(t)){
		sc = new JMenuItem("Disable abbreviation");
	    }else{
		sc = new JMenuItem("Enable abbreviation");
	    }
	}else{
	    sc = new JMenuItem("Create abbreviation");
	}
	Font myFont = UIManager.getFont(Config.KEY_FONT_TUTORIAL);
	if (myFont != null) {
		sc.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES,
				Boolean.TRUE); // Allow font to changed in JEditorPane when
							   // set to "text/html"
		sc.setFont(myFont);
	} else {
		Debug.out("KEY_FONT_TUTORIAL not available. Use standard font.");
	}
	sc.addActionListener(control);
	add(sc);
    }


    private void createNameCreationInfoSection(MenuControl control) {
	JMenuItem item = new JMenuItem("View name creation info...");
	item.addActionListener(control);
	add(item);
    }



    /** creates a non selectable label with the specified name and adds it to
     * the component followed by the entries of this section 
     * @param title a String the title of the section
     * @param taclet IList<Taclet> that contains the Taclets belonging to this section
     * @return true if section has been added (empty sections are not added)
     */ 
    private boolean addSection(String title, ImmutableList<TacletApp> taclet, 
			       MenuControl control) {
	if (taclet.size() > 0) {
	    //uncomment if you want submenus with subtitels
	    //	    insert(createSubMenu(taclet, title, control), 1);
	    //	    createSection(title);
	    add(createMenuItems(taclet, control));
	    return true;
	}
	return false;
    }

    /** inserts separator followed from the section's title 
     * @param title a String that contains the title of the section
     */
    private void createSection(String title) {
	//addSeparator();
	add(new JLabel(title));
    }
    

    private void addPopFrameItem(MenuControl control) {
	JMenuItem item = new JMenuItem("Pop method frame");
	item.addActionListener(control);
	add(item);
    }

    private void addSkolemizeTactic(MenuControl control) {
	addSeparator();
	final JMenuItem item = new JMenuItem(SKOLEMIZE_NON_RIGIDS);
	item.addActionListener(new ActionListener() {
        
        @Override
        public void actionPerformed(ActionEvent arg0) {
            SkolemizeTactic.apply(selectedGoal, mediator.getServices());
            mediator.goalChosen(selectedGoal);
        }
    });
    item.addChangeListener(new ChangeListener() {

        @Override
        public void stateChanged(ChangeEvent e) {
            MenuElement[] selectedPath = MenuSelectionManager
                    .defaultManager().getSelectedPath();
            if (selectedPath.length > 0
                    && selectedPath[selectedPath.length - 1] == item) {
                ProofAssistantController proofAssistantController = Main
                        .getInstance().getProofAssistantController();
                AIAction analyze = proofAssistantController
                        .getAssistantAI().analyze(
                                new BuiltInRuleSelectedInput("Skolemize_Non_Rigids_Tactic"));
                if (analyze != null) {
                    analyze.execute(proofAssistantController);
                }
            }
        }
    });
        Font myFont = UIManager.getFont(Config.KEY_FONT_TUTORIAL);
        if (myFont != null) {
            item.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES,
                    Boolean.TRUE); // Allow font to changed in JEditorPane when
                                   // set to "text/html"
            item.setFont(myFont);
        } else {
            Debug.out("KEY_FONT_TUTORIAL not available. Use standard font.");
        }
	add(item);
    }
    
    private void addApplyEqTactics(MenuControl control) {
        if(ApplyEquationTactic.isApplicable(pos.getPosInOccurrence())) {
            final JMenuItem item = new JMenuItem("Apply Eq Recursive Left");
            item.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    ApplyEquationTactic.apply(selectedGoal, pos.getPosInOccurrence(), true, mediator.getServices());
                    mediator.goalChosen(selectedGoal);
                }
            });
            item.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    MenuElement[] selectedPath = MenuSelectionManager
                            .defaultManager().getSelectedPath();
                    if (selectedPath.length > 0
                            && selectedPath[selectedPath.length - 1] == item) {
                        ProofAssistantController proofAssistantController = Main
                                .getInstance().getProofAssistantController();
                        AIAction analyze = proofAssistantController
                                .getAssistantAI().analyze(
                                        new BuiltInRuleSelectedInput(
                                                "applyEq"));
                        if (analyze != null) {
                            analyze.execute(proofAssistantController);
                        }
                    }
                }
            });
            add(item);
            final JMenuItem item2 = new JMenuItem("Apply Eq Recursive Right");
            item2.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    ApplyEquationTactic.apply(selectedGoal, pos.getPosInOccurrence(), false, mediator.getServices());
                    mediator.goalChosen(selectedGoal);
                }
            });
            item2.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    MenuElement[] selectedPath = MenuSelectionManager
                            .defaultManager().getSelectedPath();
                    if (selectedPath.length > 0
                            && selectedPath[selectedPath.length - 1] == item2) {
                        ProofAssistantController proofAssistantController = Main
                                .getInstance().getProofAssistantController();
                        AIAction analyze = proofAssistantController
                                .getAssistantAI().analyze(
                                        new BuiltInRuleSelectedInput(
                                                "applyEq_sym"));
                        if (analyze != null) {
                            analyze.execute(proofAssistantController);
                        }
                    }
                }
            });

            add(item2);
        }
    }
    
    private void addClipboardItem(MenuControl control) {
	addSeparator();
	JMenuItem item = new JMenuItem(TO_CLIPBOARD);
	item.addActionListener(control);
    Font myFont = UIManager.getFont(Config.KEY_FONT_TUTORIAL);
    if (myFont != null) {
        item.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES,
                Boolean.TRUE); // Allow font to changed in JEditorPane when
                               // set to "text/html"
        item.setFont(myFont);
    } else {
        Debug.out("KEY_FONT_TUTORIAL not available. Use standard font.");
    }
	add(item);
    }
    


    /** adds array of TacletMenuItem to itself*/
    private void add(TacletMenuItem[] items) {
        for (final TacletMenuItem item : items) {
            add((Component) item);
            // add listeners to show tips in the proof assistant
            ((JMenuItem) item).addChangeListener(new ChangeListener() {
                
                @Override
                public void stateChanged(ChangeEvent e) {
                    MenuElement[] selectedPath = MenuSelectionManager.defaultManager().getSelectedPath();
                    // the item can be connected to no rule if it is used to insert a hidden formula or
                    // was otherwise introduced by a taclet
                    if(selectedPath.length > 0 && selectedPath[selectedPath.length - 1] == item && item.connectedTo() != null) {
                        ProofAssistantController proofAssistantController = Main.getInstance().getProofAssistantController();
                        AIAction analyze = proofAssistantController
                                .getAssistantAI()
                                .analyze(new RuleEventInput(item.connectedTo()));
                        if(analyze != null) {
                            analyze.execute(proofAssistantController);
                        }
                    }
                }
            });
        }
    }

    /** creates new TacletMenuItems for each taclet in the list and set
     * the given MenuControl as their ActionListener
     * @param taclets IList<Taclet> with the Taclets the items represent
     * @param control the ActionListener
     * @return the new MenuItems
     */
    private TacletMenuItem[] createMenuItems(ImmutableList<TacletApp> taclets, 
					     MenuControl  control) {
	List<TacletMenuItem> items = new LinkedList<TacletMenuItem>();
	Iterator<TacletApp> it = taclets.iterator();
	
        final InsertHiddenTacletMenuItem insHiddenItem = 
            new InsertHiddenTacletMenuItem(mediator.mainFrame(), 
                    mediator.getNotationInfo(), mediator.getServices());
        
        final InsertionTacletBrowserMenuItem insSystemInvItem = 
            new InsertSystemInvariantTacletMenuItem(mediator.mainFrame(), 
                    mediator.getNotationInfo(), mediator.getServices());
       
        
        while(it.hasNext()) {
            final TacletApp app = it.next();
           
            final Taclet taclet = app.taclet();
            if (insHiddenItem.isResponsible(taclet)) {
                insHiddenItem.add(app);
            } else if (insSystemInvItem.isResponsible(taclet)) { 
                insSystemInvItem.add(app);
            } else {
                final TacletMenuItem item = 
                    new DefaultTacletMenuItem(this, app, 
                        mediator.getNotationInfo()); 
                item.addActionListener(control);
                items.add(item);                
            }        
	}
        
        if (insHiddenItem.getAppSize() > 0) {
            items.add(0, insHiddenItem);
            insHiddenItem.addActionListener(control);
        }
        
        if (insSystemInvItem.getAppSize() > 0) {
            items.add(0, insSystemInvItem);
            insSystemInvItem.addActionListener(control);
        }
        
	return items.toArray(new TacletMenuItem[items.size()]);
    }
        
    /** makes submenus invisible */
    void invisible() {
	for (int i = 0; i < getMenuComponentCount(); i++) {
	    if (getMenuComponent(i) instanceof JMenu) 
		((JMenu)getMenuComponent(i)).getPopupMenu().setVisible(false);
	}
    }
    
    /** ActionListener */
    class MenuControl implements ActionListener{

    private boolean validabbreviation(String s){
	    if(s==null || s.length()==0) return false;
	    for(int i=0; i<s.length(); i++){
		if(!((s.charAt(i)<='9' && s.charAt(i)>='0')||
		     (s.charAt(i)<='z' && s.charAt(i)>='a')||
		     (s.charAt(i)<='Z' && s.charAt(i)>='A')||
		     s.charAt(i)=='_')) return false;
	    }
	    return true;
	}

	public void actionPerformed(ActionEvent e) {
	    if (e.getSource() instanceof TacletMenuItem) {
		((SequentView)(getPopupMenu().getInvoker()))
		    .selectedTaclet(((TacletMenuItem) e.getSource()).connectedTo(), 
				    pos);
            } else if (e.getSource() instanceof BuiltInRuleMenuItem) {
        	if (((BuiltInRuleMenuItem) e.getSource()).connectedTo() instanceof SMTRule ||
        	    ((BuiltInRuleMenuItem) e.getSource()).connectedTo() instanceof SMTRuleMulti) {
        	    new DecProcRunner(Main.getInstance()
        		, selectedGoal//Main.getInstance().mediator().getProof()
        		, Main.getInstance().mediator().getProof().getUserConstraint().getConstraint()
        		, ((BuiltInRuleMenuItem) e.getSource()).connectedTo()).start();
        	} else if (e.getSource() instanceof ReduceRulesItem) {
        		mediator.selectedReduceRule((ReduceRuleApp) ((ReduceRulesItem) e.getSource()).getRuleApp());
        	} else {
                        mediator.selectedBuiltInRule
                    (((BuiltInRuleMenuItem) e.getSource()).connectedTo(), 
                     pos.getPosInOccurrence());
        	}
	    } else if (e.getSource() instanceof FocussedRuleApplicationMenuItem) {
	        mediator.getInteractiveProver ()
	            .startFocussedAutoMode ( pos.getPosInOccurrence (),
	                                     mediator.getSelectedGoal () );
	    } else {
		if (((JMenuItem)e.getSource()).getText()
		    .startsWith(TO_CLIPBOARD)){
                    Main.copyHighlightToClipboard(sequentView);
		} else if(((JMenuItem)e.getSource()).getText().
			  startsWith("Pop method frame")){
		    //                        mediator.popMethodFrame();
		} else if(((JMenuItem)e.getSource()).getText().
			  startsWith("Disable abbreviation")){
		    PosInOccurrence occ = pos.getPosInOccurrence();	    
		    if (occ != null && occ.posInTerm() != null) {
			mediator.getNotationInfo().getAbbrevMap().setEnabled(occ.subTerm(),false);
			sequentView.printSequent();
		    }
		}else if(((JMenuItem)e.getSource()).getText().
			 startsWith("Enable abbreviation")){
		    PosInOccurrence occ = pos.getPosInOccurrence();	    
		    if (occ != null && occ.posInTerm() != null) {
			mediator.getNotationInfo().
			    getAbbrevMap().setEnabled(occ.subTerm(),true);
			sequentView.printSequent();
		    }
		}else if(((JMenuItem)e.getSource()).getText().
			 startsWith("Create abbreviation")){
		    PosInOccurrence occ = pos.getPosInOccurrence();
		    if (occ != null && occ.posInTerm() != null) {
			String abbreviation = (String)JOptionPane.showInputDialog
			    (new JFrame(),
			     "Enter abbreviation for term: \n"+occ.subTerm().toString(), 
			     "New Abbreviation",
			     JOptionPane.QUESTION_MESSAGE,
			     null,
			     null,
			     "");
				    
			try{
			    if(abbreviation!=null){
				if(!validabbreviation(abbreviation)){
				    JOptionPane.showMessageDialog(new JFrame(),
								  "Only letters, numbers and '_' are allowed for Abbreviations", 
								  "Sorry",
								  JOptionPane.INFORMATION_MESSAGE);
				}else{
				    mediator.getNotationInfo().
					getAbbrevMap().put(occ.subTerm(),abbreviation,true);
				    sequentView.printSequent();
				}
			    }
			}catch(AbbrevException sce){
			    JOptionPane.showMessageDialog(new JFrame(), sce.getMessage(), "Sorry",
							  JOptionPane.INFORMATION_MESSAGE);
			}
		    }

		}else if(((JMenuItem)e.getSource()).getText().
			 startsWith("Change abbreviation")){
		    PosInOccurrence occ = pos.getPosInOccurrence();
		    if (occ != null && occ.posInTerm() != null) {
			String abbreviation = (String)JOptionPane.showInputDialog
			    (new JFrame(),
			     "Enter abbreviation for term: \n"+occ.subTerm().toString(),
			     "Change Abbreviation",
			     JOptionPane.QUESTION_MESSAGE,
			     null,
			     null,
			     mediator.getNotationInfo().
			     getAbbrevMap().getAbbrev(occ.subTerm()).substring(1));
			try{
			    if(abbreviation!=null){
				if(!validabbreviation(abbreviation)){
				    JOptionPane.showMessageDialog(new JFrame(),
								  "Only letters, numbers and '_' are allowed for Abbreviations",
								  "Sorry",
								  JOptionPane.INFORMATION_MESSAGE);
				}else{
				    mediator.getNotationInfo().
					getAbbrevMap().changeAbbrev(occ.subTerm(),abbreviation);
				    sequentView.printSequent();
				}
			    }
			}catch(AbbrevException sce){
			    JOptionPane.showMessageDialog(new JFrame(), sce.getMessage(), "Sorry",
							  JOptionPane.INFORMATION_MESSAGE);
			}
		    }
		} else if(((JMenuItem)e.getSource()).getText().
			 startsWith("View name creation info")) {
		    Term t = pos.getPosInOccurrence().subTerm();
		    ProgramVariable var = (ProgramVariable)t.op();
		    ProgramElementName name = var.getProgramElementName();
		    NameCreationInfo info = name.getCreationInfo();
		    String message;
		    if(info != null) {
			message = info.infoAsString();
		    } else {
		        message = "No information available.";
		    }
		    JOptionPane.showMessageDialog(null,
		    				  message,
						  "Name creation info",
		  				  JOptionPane.INFORMATION_MESSAGE);
		}
	    }
	}
    }



    static class FocussedRuleApplicationMenuItem extends JMenuItem {
        public FocussedRuleApplicationMenuItem () {
            super("Apply rules automatically here");
            setToolTipText("<html>Initiates and restricts automatic rule applications on the " +
                        "highlighted formula, term or sequent.<br> "+
                        "'Shift + left mouse click' on the highlighted " +
                        "entity does the same.</html>");
            Font myFont = UIManager.getFont(Config.KEY_FONT_TUTORIAL);
            if (myFont != null) {
                putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES,
                        Boolean.TRUE); // Allow font to changed in JEditorPane
                                       // when set to "text/html"
                setFont(myFont);
            } else {
                Debug.out("KEY_FONT_TUTORIAL not available. Use standard font.");
            }
        }
               
    }
    
    
    static class TacletAppComparator implements Comparator<TacletApp> {

	private int countFormulaSV(TacletSchemaVariableCollector c) {
	    int formulaSV = 0;
	    Iterator<SchemaVariable> it = c.varIterator();
	    while (it.hasNext()) {
		SchemaVariable sv = it.next();
		if(sv instanceof SortedSchemaVariable) {
		    if (((SortedSchemaVariable)sv).sort() == Sort.FORMULA) {
			formulaSV++;
		    }
		}
	    }
	
	    return formulaSV;
	}
	
	/** this is a rough estimation about the goal complexity. The
	 * complexity depends on the depth of the term to be replaced.
	 * If no such term exists we add a constant (may be refined in
	 * future)
	 */
	private int measureGoalComplexity(ImmutableList<TacletGoalTemplate> l) {
	    int result = 0;
        for (final TacletGoalTemplate gt : l) {
            if (gt instanceof RewriteTacletGoalTemplate) {
                if (((RewriteTacletGoalTemplate) gt).replaceWith() != null) {
                    result += ((RewriteTacletGoalTemplate) gt).replaceWith().depth();
                }
            }
            if (!gt.sequent().isEmpty()) {
                result += 10;
            }
        }
	    return result;
	}
	
	
	/**
	 * rough approximation of the program complexity
	 */
	public int programComplexity(JavaBlock b) {
	    if (b.isEmpty()) {
		return 0;
	    }
	    return new de.uka.ilkd.key.java.visitor.JavaASTWalker(b.program()) { 
		    private int counter = 0;
			    
		    protected void doAction(ProgramElement pe) {                      
                        counter++;
		    }		
                    
		    public int getCounter() {
			counter = 0;
			start();
			return counter;
		    }
		}.getCounter();
	}
	
	public int compare(TacletApp o1, TacletApp o2) {
	    final Taclet taclet1 = o1.taclet();
	    final Taclet taclet2 = o2.taclet();
		
            int formulaSV1 = 0;
            int formulaSV2 = 0;

            int cmpVar1 = taclet1.getRuleSets().size();
            int cmpVar2 = taclet2.getRuleSets().size();

	    if (taclet1 instanceof FindTaclet && taclet2 instanceof FindTaclet) {
	        final Term find1 = ((FindTaclet) taclet1).find();
	        int findComplexity1 = find1.depth();
	        final Term find2 = ((FindTaclet) taclet2).find();
	        int findComplexity2 = find2.depth();
	        findComplexity1 += programComplexity(find1.javaBlock());
	        findComplexity2 += programComplexity(find2.javaBlock());

	        if ( findComplexity1 < findComplexity2 ) {
	            return -1;
	        } else if (findComplexity1 > findComplexity2) {
	            return 1;
	        }		    		    		    
	        // depth are equal. Number of schemavariables decides
	        TacletSchemaVariableCollector coll1 = new TacletSchemaVariableCollector();
	        find1.execPostOrder(coll1);
	        formulaSV1 = countFormulaSV(coll1);

	        TacletSchemaVariableCollector coll2  = new TacletSchemaVariableCollector();
	        find2.execPostOrder(coll2);
	        formulaSV2 = countFormulaSV(coll2);
	        cmpVar1 += -coll1.size();
	        cmpVar2 += -coll2.size();

	    } else if (taclet1 instanceof FindTaclet != taclet2 instanceof FindTaclet) {
	        if (taclet1 instanceof FindTaclet) {
	            return -1;
	        } else {
	            return 1;
	        }
	    }

	    if (cmpVar1 == cmpVar2) {
		cmpVar1 = cmpVar1-formulaSV1;
		cmpVar2 = cmpVar2-formulaSV2;
	    }
		    
	    if ( cmpVar1 < cmpVar2 ) {
		return -1;
	    } else if (cmpVar1 > cmpVar2) {
		return 1;
	    }
	
	    if (taclet1.ifSequent().isEmpty() && 
		!taclet2.ifSequent().isEmpty()) {
		return 1;
	    } else if (!taclet1.ifSequent().isEmpty() && 
		       taclet2.ifSequent().isEmpty()) {
		return -1;
	    }
		    
	    int goals1 = -taclet1.goalTemplates().size();
	    int goals2 = -taclet2.goalTemplates().size();
	
	    if (goals1 == goals2) {
		goals1 = -measureGoalComplexity(taclet1.goalTemplates());
		goals2 = -measureGoalComplexity(taclet2.goalTemplates());		
	    } 
	
	    if (goals1 < goals2) {
		return -1;
	    } else if (goals1 > goals2) {
		return 1;
	    }
	
	    return 0;
	}
    }
}
