package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.ast.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SymbolTable {
            class TableEntry {
                NameDef nameDef;
                int scopeId;
                TableEntry previous;

                public TableEntry(NameDef nameDef, int scopeId, TableEntry previous) {
                    this.nameDef = nameDef;
                    this.scopeId = scopeId;
                    this.previous = previous;
                }
            }

            private Map<String, TableEntry> table = new HashMap<>();
            private Stack<Integer> scopeStack = new Stack<>();
            private int currentScopeId = 0;

            void enterScope() {
                scopeStack.push(currentScopeId++);
            }

            void leaveScope() {
                scopeStack.pop();
                //System.out.println(currentScopeId);
            }

            void add(String name, NameDef nameDef) {
                table.put(name, new TableEntry(nameDef, scopeStack.peek(), table.get(name)));
            }

            NameDef lookup(String name) {
                TableEntry entry = table.get(name);
                while (entry != null && !scopeStack.contains(entry.scopeId)) {
                    entry = entry.previous;
                }
                return entry != null ? entry.nameDef : null;
            }
        }