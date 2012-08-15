" Vim syntax file
" Language: KeYmaera input files
" Maintainer: Jan-David Quesel
" LAtest Revision: 14 August 2012

if exists("b:keymaera")
	finish
endif

" Keywords
syn keyword declarationKeywords \functions \problem \programVariables \sorts
syn keyword programKeywords if fi then else while elihw


syntax match lOperator "\v\\\["
syntax match lOperator "\v\\\]"
syntax match lOperator "\v\\\<"
syntax match lOperator "\v\\\>"
syn region box start="\v\\\[" end="\v\\\]" fold transparent contains=defName,lOperator,potionOperator,keymaeraComment,number,programKeywords,realType,op
syn region dia start="\v\\\<" end="\v\\\>" fold transparent contains=defName,lOperator,potionOperator,keymaeraComment,number,programKeywords,realType,op

syn region block start="{" end="}" fold transparent contains=ALL

syn keyword realType R nextgroup=defName skipwhite
syn match defName "[a-zA-Z_][a-zA-Z0-9_]*" contained

syn match number '\d\+'
syn match number '[-+]\d\+'
syn match number '\d\+\.\d*'
syn match number '[-+]\d\+\.\d*'

syn match keymaeraLineComment "//.*"
syn region keymaeraComment start="/\*" end="\*/"

syn match op "\v\<\="
syn match op "\v\>\="
syn match op "\v\="
syn match op "\v:\="
syn match op "\v\?"
syn match op "\v\*"
syn match op "\v/"
syn match op "\v\+"
syn match op "\v-"

syn match lOperator '\\forall'
syn match lOperator '\\exists'
syn match lOperator '&'
syn match lOperator '|'
syn match lOperator '!'
syn match lOperator "\v-\>"
syn match lOperator "\v\<-\>"

hi def link realType Type
hi def link defName Function
hi def link declarationKeywords Keyword
hi def link programKeywords Statement
hi def link number Constant
hi def link lOperator Operator
hi def link op Operator
hi def link keymaeraComment Comment
hi def link keymaeraLineComment Comment
