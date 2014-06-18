
(* Copyright (C) Andrew Sogokon and Khalil Ghorbal 2014 *)
(* email. khalil.ghorbal@gmail.com *)
(* This package is released under GPL license. Please read the COPYRIGHT file *)

(* ::Package:: *)

BeginPackage["Invariants`"]

SOSToList::usage="SOSToList[sos_] Turn a sum-of-squares into a list (real equivalence, if interpreted as a conjunction)."

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

DRIBuggy::usage="DRI[h_,Lx_,vars_] Differential Radical Invariant. Lx: polynomial vector field, h: polynomial invariant candidate, vars: state variables"

SFDRI::usage="SFDRI[h_,Lx_,vars_] Differential Radical Invariant after SF reduction. Lx: polynomial vector field, h: polynomial invariant candidate, vars: state variables"

SoSDRI::usage="SoSDRI[h_,Lx_,vars_] Differential Radical Invariant. Lx: polynomial vector field, h: a list of polynomials, vars: state variables"

DRIConj::usage="DRIConj[h_List, Lx_, vars_] Conjunctive Differential Radical Invariant. Lx: polynomial vector field, h: polynomial invariant candidate variety, vars: state variables"

Darboux::usage="Darboux[h_,Lx_,vars_] Darboux Polynomials (Rational Integrals). Lx: polynomial vector field, h: polynomial invariant candidate, vars: state variables"

SFDarboux::usage="Darboux[h_,Lx_,vars_] Darboux Polynomials after SF reduction. Lx: polynomial vector field, h: polynomial invariant candidate, vars: state variables"

Lie::usage="Lie[p_,f_,vars_List] Lie's criterion for invariance of smooth differentiable manifolds."

SFLie::usage="SFLie[p_,f_,vars_List] Lie's criterion applied on a square-free reduced invariant candidate."

SFFalsify::usage="SFFalsify[p_,f_,vars_List] Lie's criterion applied on a square-free reduced invariant candidate to falsify the invariance claim. This method does NOT perform the full rank gradient check and is therefore unsound when it returns true. If the method returns False, the candidate is not an invariant."

SFLieZero::usage="SFLieZero[p_,f_,vars_List] Modified Lie's criterion applied on a square-free reduced invariant candidate. Checks that there is no flow on the singular locus."

SFLieStar::usage="SFLieStar[p_,f_,vars_List] Modified Lie's criterion applied on a square-free reduced invariant candidate. Generalises SFLieZero"


Begin["`Private`"]

SOSToList[sos_]:=Module[{},Map[Function[x,x/.{Power[a_,2] -> a}],If[TrueQ[Head[sos]==Plus],Apply[List,sos],{sos}]]]

PolynomialDegree[p_,vars_List]:=Module[{},
Max[Map[Plus @@ # &, CoefficientRules[p, vars][[All, 1]]]]
]

(* Expects a list of polynomials *)
VectorFieldDegree[f_List,vars_List]:=Module[{},
Max[Map[PolynomialDegree[#, vars]&,f]]
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

(* Buggy CAV version *)
DRIBuggy[h_, Lx_, vars_] := Catch[Module[{N = 1, SFh=SF[h], L, GB, LX = Lx /. {Rule[a_, b_] -> b}, Remainder, Reduction = True},
L = {h};
While[True,
  If[N<2,Remainder = PolynomialReduce[LieDerivative[h, Lx, vars], {h}, vars][[2]],
  GB = GroebnerBasis[L, vars, 
  MonomialOrder -> DegreeReverseLexicographic];
  Remainder = PolynomialReduce[L[[-1]], GB, vars][[2]];
  ]
  If[PossibleZeroQ[Remainder], Throw[{True,StringForm["(N=``)", N]}],
   AppendTo[L, LieDerivative[L[[N]], Lx, vars]];
   Reduction = Reduction && TrueQ[Reduce[UniversalClosure[Implies[SFh == 0, SF[L[[-1]]] == 0], vars], vars, Reals]];
   If[Not[Reduction], Throw[{False,StringForm["(N=``)", N]}], N++]
  ]
 ]
]]

(* uses SF reduction for the QE problem but not for the ideal membership *)
DRI[h_, Lx_, vars_] := Catch[Module[{lieD, N = 1, SFh=SF[h], L={h}, GB, Remainder, Reduction = True,allvars=Variables[Join[{h},Lx]]},
While[True,
  lieD=LieDerivative[L[[N]], Lx, vars];
  If[N<2,Remainder = PolynomialReduce[lieD, {h}, vars][[2]],
  GB = GroebnerBasis[L, vars, 
  MonomialOrder -> DegreeReverseLexicographic];
  Remainder = PolynomialReduce[lieD, GB, vars,MonomialOrder -> DegreeReverseLexicographic][[2]];
  ]
  If[PossibleZeroQ[Remainder], Throw[{True,StringForm["(N=``)", N]}],
   AppendTo[L, lieD];
   Reduction = Reduction && TrueQ[Reduce[UniversalClosure[Implies[SFh == 0, SF[L[[-1]]] == 0], allvars], allvars, Reals]];
   If[Reduction, N++, Throw[{False,StringForm["(N=``)", N]}]]
  ]
 ]
]]

SoSDRI[V_List, Lx_, vars_]:=Module[{h=SumOfSquares[V]},
If[Length[V]==1,DRI[V[[1]],Lx,vars],DRI[h, Lx, vars]]
]

(* uses SF reduction everywhere *)
SFDRI[h_, Lx_, vars_] := Module[{},DRI[SF[h],Lx,vars]]

DRIConj[h_List, Lx_, vars_] := Catch[Module[{N = 1,r = Length[h], conj, range, L=h, GB, RemainderZQ=False, Reduction = True,tmp={},GBL=h, allvars=Variables[Join[h,Lx]]},
If[r==1,DRI[h[[1]],Lx,vars],
range=Range[1,r];
conj=And@@Flatten[Reap[Do[Sow[SF[L[[i]]] == 0], {i, 1, r}]][[2]]];
While[True,
  GB = GroebnerBasis[GBL, vars, MonomialOrder -> DegreeReverseLexicographic];
  tmp =  Map[LieDerivative[L[[-#]], Lx, vars] &, range];
  L = Join[L,tmp];
  RemainderZQ = And@@Map[PossibleZeroQ[PolynomialReduce[L[[-#]], GB, vars,MonomialOrder -> DegreeReverseLexicographic][[2]]]&,range];
  If[RemainderZQ, Throw[{True,StringForm["(N=``)", N]}],
   GBL = Join[GB, tmp];
   Reduction = Reduction && And@@Map[TrueQ[Reduce[UniversalClosure[Implies[conj, SF[L[[-#]]] == 0], allvars], allvars, Reals]]&,range];
   If[Reduction, N++, Throw[{False,StringForm["(N=``)", N]}]];
  ]
 ]
]]]

Darboux[h_, Lx_, vars_] := Catch[Module[{Remainder},
  Remainder = PolynomialReduce[LieDerivative[h, Lx, vars], {h}, vars][[2]];
  If[PossibleZeroQ[Remainder], Throw[True], Throw[False]]
]]

SFDarboux[h_, Lx_, vars_] := Module[{SFh=SF[h]},Darboux[SFh,Lx,vars]]

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

SFLieStar[p_,f_,vars_List]:=Module[{SFp = SF[p],P,TAU},
P=SFp/.{Map[Function[v,Rule[v,v+TAU*Derivative[1][v]]], vars]};
UniversalClosure[
Implies[SFp==0,LieDerivative[SFp,f,vars]==0  && Implies[SingularLocus[SFp,vars], (P/.f/.{Rule[a_,b_]-> Rule[F[a],b]})[[1]]==0]],
Join[vars,{TAU}]]
]

End[ ]
EndPackage[ ]



























