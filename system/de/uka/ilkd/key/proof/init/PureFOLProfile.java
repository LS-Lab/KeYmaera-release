package de.uka.ilkd.key.proof.init;

import de.uka.ilkd.key.strategy.FOLStrategy;
import de.uka.ilkd.key.strategy.SetOfStrategyFactory;
import de.uka.ilkd.key.strategy.StrategyFactory;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.NamespaceSet;

public class PureFOLProfile extends AbstractProfile {
   
    private final static StrategyFactory DEFAULT = new FOLStrategy.Factory();
    
    public PureFOLProfile() {
        super("standardRules-FOL.key");       
    }

    public String name() {
        return "Pure FOL Profile";
    }

    protected SetOfStrategyFactory getStrategyFactories() {  
        return super.getStrategyFactories().
            add(DEFAULT);
    }  
       
    public StrategyFactory getDefaultStrategyFactory() {        
        return DEFAULT;
    }

	/**
	 * As there are no programs in FOL this method returns null
	 * @return null
	 */
	public ProgramBlockProvider getProgramBlockProvider() {
		return null;
	}

	/**
	 * As there are no programs in FOL this method returns null
	 * @return null
	 */
	public ProgramBlockProvider getProgramBlockProvider(Services services, NamespaceSet namespaces) {
		return null;
	}
}
