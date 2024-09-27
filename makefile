.PHONY: clean run-lexer run-parser

make:
	@javac lexer/*.java parser/*.java

run-parser:
	@make -C parser
	@java -cp . parser.Main

run-lexer:
	@make -C lexer
	@java -cp . lexer.Main

clean:
	@make -C lexer clean
	@make -C parser clean

clean-lexer:
	@rm -f lexer/*.class

clean-parser:
	@rm -f parser/*.class
