
(* Copyright (C) Andrew Sogokon and Khalil Ghorbal 2014 *)

(* This package is released under GPL license. Please read the COPYRIGHT file *)  

(* ::Package:: *)

BeginPackage["Invariants`"]

PolynomialDegree::usage="PolynomialDegree[p_] Computes the degree of a polynomial argument."

VectorFieldDegree::usage="VectorFieldDegree[f_] Computes the degree of a polynomial vector field."

UniversalClosure::usage="UniversalClosure[F_, vars_List] Compute unversal closure of the formula F in the variables vars."

LieDerivative::usage="LieDerivative[p_,f_, vars_List] Computes the Lie derivative of a function p with respect to the vector field given by a vector function f. State variables need to be supplied explicitly as a Sequence."

FullRankGrad::usage="FullRankGrad[p_, vars_List] Check full rank condition."

AllEqualZero::usage="AllEqualZero[list_List] Compute conjunction of zero equality tests for all elements in a list."

SumOfSquares::usage="SumOfSquares[list_List] Compute a sum of squares of elements in a list."

SingularLocus::usage="SingularLocus[p_, vars_List] Compute a sum-of-squares corresponding to the singular locus of polynomial p."

SF::usage="SF[p_] Square-free reduction of a polynomial."

DI::usage="DI[p_,f_,vars_List] Differential Induction for equational atoms."

DRI::usage="DRI[h_,Lx_,vars_] Differential Radical Invariant. Lx: polynomial vector field, h: polynomial invariant candidate, vars: state variables"

SFDRI::usage="DRI[h_,Lx_,vars_] Differential Radical Invariant on SF reduced candidate. Lx: polynomial vector field, h: polynomial invariant candidate, vars: state variables"

Darboux::usage="Darboux[h_,Lx_,vars_] Darboux Polynomials (Rational Integrals). Lx: polynomial vector field, h: polynomial invariant candidate, vars: state variables"

SFDarboux::usage="SFDarboux[h_,Lx_,vars_] Darboux Polynomials (Rational Integrals) on SF reduced candidate. Lx: polynomial vector field, h: polynomial invariant candidate, vars: state variables"

Lie::usage="Lie[p_,f_,vars_List] Lie's criterion for invariance of smooth differentiable manifolds."

SFLie::usage="SFLie[p_,f_,vars_List] Lie's criterion applied on a square-free reduced invariant candidate."

SFFalsify::usage="SFFalsify[p_,f_,vars_List] Lie's criterion applied on a square-free reduced invariant candidate to falsify the invariance claim. This method does NOT perform the full rank gradient check and is therefore unsound when it returns true. If the method returns False, the candidate is not an invariant."

SFLieZero::usage="SFLieZero[p_,f_,vars_List] Modified Lie's criterion applied on a square-free reduced invariant candidate. Checks that there is no flow on the singular locus."

SFLieStar::usage="SFLieStar[p_,f_,vars_List] Modified Lie's criterion applied on a square-free reduced invariant candidate. Generalises SFLieZero"


Begin["`Private`"]

PolynomialDegree[p_,vars_List]:=Module[{},
Max[Map[Plus @@ # &, CoefficientRules[p, vars][[All, 1]]]]
]

VectorFieldDegree[f_,vars_List]:=Module[{},
Max[Map[PolynomialDegree[#, vars]&,f[[All, 2]]]]
]

UniversalClosure[F_,vars_List]:=Module[{},
ForAll[vars,F]
]

LieDerivative[p_,f_, vars_List]:=Module[{},
Grad[p,vars].(Map[Derivative[1][#]&,vars]/.f)
]

FullRankGrad[p_, vars_List]:=Module[{},
Apply[Or,Map[Function[x, Unequal[x,0]],Grad[p,vars] ]]
]

SumOfSquares[list_List]:=Module[{},
Apply[Plus,Map[Function[x, x^2],list]]
]

AllEqualZero[list_List]:=Module[{},
Apply[And,Map[Function[x, x==0],list]]
]

SingularLocus[p_, vars_List]:=Module[{},
AllEqualZero[Grad[p,vars]]
]

SF[p_]:=Module[{},
Apply[Times,Map[Function[x,First[x]],FactorSquareFreeList[p]]]
]

DI[p_,f_,vars_List]:=Module[{},
UniversalClosure[
LieDerivative[p,f,vars]==0,
vars]
]

(* uses SF reduction for the QE problem but not for the ideal membership *)
DRI[h_, Lx_, vars_] := Catch[Module[{N = 1, SFh=SF[h], L, GB, LX = Lx /. {Rule[a_, b_] -> b}, Remainder, Reduction = True},
L = {h};
While[True,
  If[N<2,Remainder = PolynomialReduce[LieDerivative[h, Lx, vars], {h}, vars][[2]],
  GB = GroebnerBasis[L, vars, 
  MonomialOrder -> DegreeReverseLexicographic];
  Remainder = PolynomialReduce[L[[-1]], GB, vars][[2]];
  ]
  If[PossibleZeroQ[Remainder], Throw[True],
   AppendTo[L, LieDerivative[L[[N]], Lx, vars]];
   Reduction = Reduction && TrueQ[Reduce[UniversalClosure[Implies[SFh == 0, SF[L[[-1]]] == 0], vars], vars, Reals]];
   If[Not[Reduction], Throw[False], N++]
  ]
 ]
]]

(* uses SF reduction everywhere *)
SFDRI[h_, Lx_, vars_] := Module[{SFh=SF[h]},
DRI[SFh,Lx,vars]
]

Darboux[h_, Lx_, vars_] := Catch[Module[{LX = Lx /. {Rule[a_, b_] -> b}, Remainder},
  Remainder = PolynomialReduce[LieDerivative[h, Lx, vars], {h}, vars][[2]];
  If[PossibleZeroQ[Remainder], Throw[True], Throw[False]]
]]

(* SF reduction + Darboux *)
SFDarboux[h_, Lx_, vars_] := Module[{SFh=SF[h]},
  Darboux[SFh,Lx,vars]
]

Lie[p_,f_,vars_List]:=Module[{},
UniversalClosure[
Implies[p==0,LieDerivative[p,f,vars]==0 && FullRankGrad[p,vars]],
vars]
]

SFLie[p_,f_,vars_List]:=Module[{SFp = SF[p]},
Lie[SFp,f,vars]
]

SFFalsify[p_,f_,vars_List]:=Module[{SFp = SF[p]},
Not[UniversalClosure[
Implies[SFp==0,LieDerivative[SFp,f,vars]==0],
 vars]
]
]

SFLieZero[p_,f_,vars_List]:=Module[{SFp = SF[p]},
UniversalClosure[
Implies[SFp==0,LieDerivative[SFp,f,vars]==0  && Implies[SingularLocus[SFp,vars], AllEqualZero[(Map[Derivative[1][#]&,Intersection[Variables[SFp],vars]]/.f)]]],
vars]
]

SFLieStar[p_,f_,vars_List]:=Module[{SFp = SF[p]},
P=SFp/.{Map[Function[v,Rule[v,v+TAU*Derivative[1][v]]], vars]};
UniversalClosure[
Implies[SFp==0,LieDerivative[SFp,f,vars]==0  && Implies[SingularLocus[SFp,vars], (P/.f/.{Rule[a_,b_]-> Rule[F[a],b]})[[1]]==0]],
Join[vars,{TAU}]]
]

End[ ]
EndPackage[ ]












