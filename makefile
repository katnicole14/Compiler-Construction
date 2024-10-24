.PHONY: clean run-lexer run-parser run-lexer-parser run-symbol-table run-translation

make:
	@javac lexer/*.java parser/*.java symbol_table/*.java

run-parser:
	@java -cp . parser.Main

run-lexer:
	@java -cp lexer Main

run-lexer-parser:
	@make run-lexer
	@make run-parser

run-symbol-table:
	@java -cp . symbol_table.SymbolTableMain
run-translation:
	@java -cp . translation.Main

clean:
	@rm -f lexer/*.class parser/*.class symbol_table/*.class translation/*.class

	
