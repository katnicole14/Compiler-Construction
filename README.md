# Compiler-Construction

## to run executable , you will find then named compiler.jar

## Overview
This project consists of a **Lexer** and a **Parser** as part of a compiler construction project. You can compile, run, and clean the project using the `make` commands provided below.

## Requirements
- Java Development Kit (JDK) 11 or higher
- GNU Make

## Project Structure
- **Lexer**: Contains the lexer component for tokenizing the input.
- **Parser**: Contains the parser component for syntax analysis of the tokens generated by the lexer.

## How to Use

### Run the Lexer
to run the .zip file follow these steps:
Navigate to the root directory of the project and run the following command:
```bash
make 



```bash
make run-lexer

 "You will get a response that it was successful and created the output is under lexer/output.xml"

### Run the Lexer
run lexer first then parser 

"tree will pop up if you want to see gui or JAVA visually "
"if you cant the tree will still appear on the terminal or you can view syntax_tree.xml in root directory"
"if you want to feed your own .xml to parser place it under lexer folder and name it lexer/output.xml"

```bash




### run symbol table plus type 

make run-symbol-table 
"table will appear on the terminal allong with semantic errors if there are any errors"
"also has the type checker with input "
"allChecksPass=true shows a tree with successful type checking and false for otherwise"

### cleanup 
```bash
make clean
