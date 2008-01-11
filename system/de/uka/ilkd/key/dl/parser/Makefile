ifndef BASE
	BASE=/home/stud/jdq/doc/da/src
endif

ifndef ANTLR_PREFIX
	ANTLR_PREFIX=${BASE}/key-ext-jars/antlr-3.0b5/lib
endif

ifndef KEY_HOME
	KEY_HOME=${BASE}/system/binary
endif

TREEPARSER=$(patsubst %Stage2.g,%Stage2TreeParser.java,$(wildcard *Stage2.g))
TMPPARSER=$(patsubst %.g,%Parser.java,$(wildcard *.g))
TMPLEXER=$(patsubst %.g,%Lexer.java,$(wildcard *.g))
PARSER_FILTERED=$(subst Stage2,,$(TMPPARSER))
PARSER_FILTERED2=$(subst __,,$(PARSER_FILTERED))
PARSER=$(shell echo $(PARSER_FILTERED2) | sed -e 's/ /\n/g' | uniq | sed -e 's/\n/ /g')
LEXER_FILTERED=$(subst Stage2,,$(TMPLEXER))
LEXER_FILTERED2=$(subst __,,$(LEXER_FILTERED))
LEXER=$(shell echo $(LEXER_FILTERED2) | sed -e 's/ /\n/g' | uniq | sed -e 's/\n/ /g')
LEXER_CLASS=$(patsubst %.java,%.class,$(LEXER))
PARSER_CLASS=$(patsubst %.java,%.class,$(PARSER))
TREEPARSER_CLASS=$(patsubst %.java,%.class,$(TREEPARSER))

test:
	java -cp ${ANTLR_PREFIX}/antlr-2.7.7.jar:${ANTLR_PREFIX}/stringtemplate-3.0.jar:${ANTLR_PREFIX}/antlr-3.0b5.jar org.antlr.Tool --help

%Stage2TreeParser.java: %Stage2.g
	java -cp ${ANTLR_PREFIX}/antlr-2.7.7.jar:${ANTLR_PREFIX}/stringtemplate-3.0.jar:${ANTLR_PREFIX}/antlr-3.0b5.jar org.antlr.Tool $<

%Parser.java: %.g
	java -cp ${ANTLR_PREFIX}/antlr-2.7.7.jar:${ANTLR_PREFIX}/stringtemplate-3.0.jar:${ANTLR_PREFIX}/antlr-3.0b5.jar org.antlr.Tool $<

%.class: %.java
	javac -d ${KEY_HOME} -cp ${ANTLR_PREFIX}/antlr-2.7.7.jar:${ANTLR_PREFIX}/stringtemplate-3.0.jar:${ANTLR_PREFIX}/antlr-3.0b5.jar:${KEY_HOME} $<

all: $(PARSER_CLASS) $(TREEPARSER_CLASS) $(LEXER_CLASS) ast
	mkdir -p ${KEY_HOME}/de/uka/ilkd/key/parser/dl

ast: $(wildcard ../../dl/*.java) $(wildcard ../../dl/impl/*.java)
	cd ../../dl; \
	javac -d ${KEY_HOME} -cp ${ANTLR_PREFIX}/antlr-2.7.7.jar:${ANTLR_PREFIX}/stringtemplate-3.0.jar:${ANTLR_PREFIX}/antlr-3.0b5.jar:${KEY_HOME}:. *.java; 
		
run:
	java -cp ${ANTLR_PREFIX}/antlr-2.7.7.jar:${ANTLR_PREFIX}/stringtemplate-3.0.jar:${ANTLR_PREFIX}/antlr-3.0b5.jar:${KEY_HOME}:. de.uka.ilkd.key.parser.dl.Main
