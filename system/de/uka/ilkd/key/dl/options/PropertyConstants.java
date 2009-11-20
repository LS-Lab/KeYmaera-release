/***************************************************************************
 *   Copyright (C) 2008 by Jan-David Quesel                                *
 *   quesel@informatik.uni-oldenburg.de                                    *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/
package de.uka.ilkd.key.dl.options;

/**
 * This class stores the property keys used for referencing properties within
 * property files. It is used by the option beans to know how to load and save
 * their respective properties and by the initial configuration dialog which is
 * invoked prior to starting the complete key system and thus before
 * initializing the property beans.
 * 
 * @author jdq
 */
public interface PropertyConstants {

	/**
	 * 
	 */
	static final String DLOPTIONS_INITIAL_TIMEOUT = "[DLOptions]initialTimeout";
	static final String DLOPTIONS_DIFFSAT_TIMEOUT = "[DLOptions]diffSatTimeout";
	static final String DLOPTIONS_LOOPSAT_TIMEOUT = "[DLOptions]loopSatTimeout";
	/**
	 * 
	 */
	static final String DLOPTIONS_FO_STRATEGY = "[DLOptions]FOStrategy";
	static final String DLOPTIONS_QUADRIC = "[DLOptions]quadricTimeoutIncreaseFactor";
	static final String DLOPTIONS_LINEAR = "[DLOptions]linearTimeoutIncreaseFactor";
	static final String DLOPTIONS_CONSTANT = "[DLOptions]constantTimeoutIncreaseFactor";
	static final String DLOPTIONS_READD_QUANTIFIERS = "[DLOptions]readdQuantifiers";
	static final String DLOPTIONS_SIMPLIFY_BEFORE_REDUCE = "[DLOptions]simplifyBeforeReduce";
	static final String DLOPTIONS_SIMPLIFY_AFTER_REDUCE = "[DLOptions]simplifyAfterReduce";
	static final String DLOPTIONS_APPLY_UPDATES_TO_MODALITIES = "[DLOptions]applyToModality";
	static final String DLOPTIONS_COUNTEREXAMPLE_GENERATOR = "[DLOptions]counterExampleGenerator";
	static final String DLOPTIONS_ODESOLVER = "[DLOptions]odeSolver";
	static final String DLOPTIONS_QUANTIFIER_ELIMINATOR = "[DLOptions]quantifierEliminator";
	static final String DLOPTIONS_SIMPLIFIER = "[DLOptions]simplifier";
	static final String DLOPTIONS_APPLY_GAMMA_RULES = "[DLOptions]applyGammaRules";
	static final String DLOPTIONS_COUNTEREXAMPLE_TEST = "[DLOptions]counterexampleTest";
	static final String DLOPTIONS_INVARIANT_RULE = "[DLOptions]invariantRule";
	static final String DLOPTIONS_USE_DIFF_SAT = "[DLOptions]DiffSat";
	static final String DLOPTIONS_IGNORE_ANNOTATIONS = "[DLOptions]ignoreAnnotations";
	static final String DLOPTIONS_SIMPLIFY_TIMEOUT = "[DLOptions]simplifyTimeout";
	static final String DLOPTIONS_ITERATIVE_REDUCE_RULE = "[DLOptions]useIterativeReduceRule";
	static final String DLOPTIONS_TERM_FACTORY_CLASS = "[DLOptions]termFactoryClass";
	static final String DLOPTIONS_APPLY_LOCAL_REDUCE = "[DLOptions]applyLocalReduce";
	static final String DLOPTIONS_APPLY_LOCAL_SIMPLIFY = "[DLOptions]applyLocalSimplify";
	static final String DLOPTIONS_APPLY_GLOBAL_REDUCE = "[DLOptions]applyGlobalReduce";
	static final String DLOPTIONS_SIMPLIFY_AFTER_ODESOLVE = "[DLOptions]simplifyAfterODESolve";
	static final String DLOPTIONS_GROEBNER_BASIS_CALCULATOR = "[DLOptions]groebnerBasisCalculator";
	static final String DLOPTIONS_SOS_CHECKER = "[DLOptions]sosChecker";
	static final String DLOPTIONS_USE_POWERSET_ITERATIVE_REDUCE = "[DLOptions]usePowersetIterativeReduce";
	static final String DLOPTIONS_PERCENT_OF_POWERSET_FOR_ITERATIVE_REDUCE = "[DLOptions]percentOfPowersetForIterativeReduce";
	static final String DLOPTIONS_BUILT_IN_ARITHMETIC = "[DLOptions]BuiltInArithmetic";
	static final String DLOPTIONS_BUILT_IN_ARITHMETIC_INEQS = "[DLOptions]BuiltInArithmeticIneqs";
	static final String DLOPTIONS_USE_SOS = "[DLOptions]useSOS";

	static final String DLOPTIONS_CSDP_PATH = "[DLOptions]csdpPath";
	static final String DLOPTIONS_CSDP_PATH_LABEL = "csdp binary";
	static final String DLOPTIONS_CSDP_PATH_TOOLTIP = "The path to the csdp binary file. (Used by groebnerSOS and internal sos)";

	static final String DLOPTIONS_CSDP_FORCE_INTERNAL = "[DLOptions]csdpForceInternal";
	
	static final String HOL_OPTIONS_HOLLIGHT_PATH = "[HOLLightOptions]hollightPath";
	static final String HOL_OPTIONS_HOLLIGHT_PATH_LABEL = "HOL Light Path";
	static final String HOL_OPTIONS_HOLLIGHT_PATH_TOOLTIP = "The path to the hol light installation needed to setup the correct environment for the tool";

	static final String HOL_OPTIONS_OCAML_PATH = "[HOLLightOptions]ocamlPath";
	static final String HOL_OPTIONS_OCAML_PATH_LABEL = "Ocaml Path";
	static final String HOL_OPTIONS_OCAML_PATH_TOOLTIP = "The ocaml binary";

	static final String HOL_OPTIONS_HARRISON_QE_PATH = "[HOLLightOptions]harrisonqePath";
	static final String HOL_OPTIONS_HARRISON_QE_PATH_LABEL = "Harrison QE Path";
	static final String HOL_OPTIONS_HARRISON_QE_PATH_TOOLTIP = "The path to harrisons implementation of quantifier elimination";
	
	static final String HOL_OPTIONS_QUANTIFIER_ELIMINATION_METHOD = "[HOLLightOptions]qeMethod";	
	static final String MATHEMATICA_OPTIONS_QUANTIFIER_ELIMINATION_METHOD = "[MathematicaOptions]quantifierEliminationMethod";
	static final String MATHEMATICA_OPTIONS_USE_ELIMINATE_LIST = "[MathematicaOptions]useEliminateList";
	static final String MATHEMATICA_OPTIONS_MEMORYCONSTRAINT = "[MathematicaOptions]memoryConstraint";
	static final String MATHEMATICA_OPTIONS_CONVERT_DECIMAL_FRACTIONS_TO_RATIONALS = "[MathematicaOptions]convertDecimalFractionsToRationals";

	static final String MATHEMATICA_OPTIONS_MATHKERNEL = "[MathematicaOptions]mathKernel";

	static final String MATHEMATICA_OPTIONS_MATHKERNEL_LABEL = "MathKernel path";
	static final String MATHEMATICA_OPTIONS_MATHKERNEL_TOOLTIP = "the path to the MathKernel binary";

	static final String MATHEMATICA_OPTIONS_JLINK_LIBDIR = "com.wolfram.jlink.libdir";

	static final String MATHEMATICA_OPTIONS_JLINK_LIBDIR_LABEL = "J/Link native dir";
	static final String MATHEMATICA_OPTIONS_JLINK_LIBDIR_TOOLTIP = "the path where the J/Link natives are located. Restart is required when this setting is changed.";

	static final String ORBITAL_OPTIONS_REPRESENTATION = "[OrbitalOptions]representation";
	static final String ORBITAL_OPTIONS_SPARSEPOLYNOMIALS = "[OrbitalOptions]sparsePolynomials";
	static final String ORBITAL_OPTIONS_PRECISION = "[OrbitalOptions]precision";

	static final String QEPCAD_OPTIONS_QEPCAD_PATH = "[QepcadOptions]qepcadPath";
	static final String QEPCAD_OPTIONS_QEPCAD_PATH_LABEL = "Qepcad Path";
	static final String QEPCAD_OPTIONS_QEPCAD_PATH_TOOLTIP = "The path to the qepcad installation needed to setup the correct environment for the tool (it must contain bin/qepcad binary)";

	static final String QEPCAD_OPTIONS_SACLIB_PATH = "[QepcadOptions]saclibPath";
	static final String QEPCAD_OPTIONS_SACLIB_PATH_LABEL = "Saclib Path";
	static final String QEPCAD_OPTIONS_SACLIB_PATH_TOOLTIP = "The path to the saclib installation needed to setup the correct environment for Qepcad";

	static final String QEPCAD_OPTIONS_QEPCAD_MEMORYLIMIT = "[QepcadOptions]qepcadMemoryLimit";

	static final String OPTIONS_REDUCE_BINARY = "[ReduceOptions]reduceBinary";
	static final String OPTIONS_REDUCE_BINARY_LABEL = "Reduce Binary";
	static final String OPTIONS_REDUCE_BINARY_TOOLTIP = "<html>The path to the reduce binary installation needed<br>"
			+ "to setup the correct environment for the tool</html>";

	static final String OPTIONS_REDUCE_QUANTIFIER_ELIMINATION_METHOD = "[ReduceOptions]quantifierEliminationMethod";
	static final String OPTIONS_REDUCE_ELIMINATE_FRACTIONS = "[ReduceOptions]eliminateFractions";
	static final String OPTIONS_REDUCE_RLALL = "[ReduceOptions]rlall";
	static final String OPTIONS_REDUCE_rlanuexsgnopt = "[ReduceOptions]rlanuexsgnopt";
	static final String OPTIONS_REDUCE_rlanuexgcdnormalize = "[ReduceOptions]ReduceSwitch rlanuexgcdnormalize";
	static final String OPTIONS_REDUCE_rlanuexpsremseq = "[ReduceOptions]rlanuexpsremseq";
	static final String OPTIONS_REDUCE_rlcadhongproj = "[ReduceOptions]rlcadhongproj";
	static final String OPTIONS_REDUCE_rlcadaprojalways = "[ReduceOptions]rlcadaprojalways";
	static final String OPTIONS_REDUCE_rlcadaproj = "[ReduceOptions]rlcadaproj";
	static final String OPTIONS_REDUCE_rlcadisoallroots = "[ReduceOptions]rlcadisoallroots";
	static final String OPTIONS_REDUCE_rlcadrawformula = "[ReduceOptions]rlcadrawformula";
	static final String OPTIONS_REDUCE_rlcadtrimtree = "[ReduceOptions]rlcadtrimtree";
	static final String OPTIONS_REDUCE_rlcadfulldimonly = "[ReduceOptions]rlcadfulldimonly";
	static final String OPTIONS_REDUCE_rlcadpbfvs = "[ReduceOptions]rlcadpbfvs";
	static final String OPTIONS_REDUCE_rlcadte = "[ReduceOptions]rlcadte";
	static final String OPTIONS_REDUCE_rlcadpartial = "[ReduceOptions]rlcadpartial";
	static final String OPTIONS_REDUCE_rlcadextonly = "[ReduceOptions]rlcadextonly";
	static final String OPTIONS_REDUCE_rlcadprojonly = "[ReduceOptions]rlcadprojonly";
	static final String OPTIONS_REDUCE_rlcadbaseonly = "[ReduceOptions]rlcadbaseonly";
	static final String OPTIONS_REDUCE_rlcadfac = "[ReduceOptions]rlcadfac";
	static final String OPTIONS_REDUCE_rlqepnf = "[ReduceOptions]rlqepnf";
	static final String OPTIONS_REDUCE_rlqeheu = "[ReduceOptions]rlqeheu";
	static final String OPTIONS_REDUCE_rlqedfs = "[ReduceOptions]rlqedfs";
	static final String OPTIONS_REDUCE_rlqesqsc = "[ReduceOptions]rlqesqsc";
	static final String OPTIONS_REDUCE_rlqeqsc = "[ReduceOptions]rlqeqsc";
	static final String OPTIONS_REDUCE_RLSIMPL = "[ReduceOptions]rlsimpl";

}
