package com.sk.bds.datainsight.util;


import jdk.nashorn.internal.objects.annotations.Where;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.SimpleNode;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.values.ValuesStatement;

import java.util.ArrayList;
import java.util.List;

import static org.apache.coyote.http11.Constants.a;

public class SQLParser implements SelectVisitor {

    public List<SelectItem> selectItems = new ArrayList<>();
    public List<OrderByElement> orderByElements = new ArrayList<>();
    private Expression whereItem;

    public SQLParser() {

    }

    public SQLParser(Expression whereItem) {
        this.whereItem = whereItem;
    }

    private Expression getWhereItem() {
        return this.whereItem;
    }

    @Override
    public void visit(PlainSelect plainSelect) {

        System.out.println("plain select visit!");
        System.out.println(plainSelect);
        // get select items
        System.out.println("select items =");
        System.out.println(this.getWhereItem());
        if (plainSelect.getWhere() == null) {
            plainSelect.setWhere(getWhereItem());
            return;
        }
        plainSelect.setWhere(new AndExpression(plainSelect.getWhere(), this.getWhereItem()));
//        WhereParser whereParser = new WhereParser(getWhereItem(), plainSelect);
//        plainSelect.getWhere().accept(whereParser);
//        System.out.println(plainSelect);
/*        plainSelect.getSelectItems().forEach(e -> {
            //System.out.println(e.toString());
            selectItems.add(e);
        });*/
        // process subquery
        //SubQueryParser sqp = new SubQueryParser();
        //plainSelect.getFromItem().accept(sqp);

        // get & process subquery result
/*        if (!sqp.getOrderByElements().isEmpty()) {
            // TODO multiple columns
            //Function col = (Function) sqp.getOrderByElements().get(0).getExpression();
            //Map<String, String> outerColName = new TreeMap<>();
            //selectItems.forEach(v -> outerColName.put(((Column)((SelectExpressionItem)v).getExpression()).getColumnName(), String.valueOf(((SelectExpressionItem) v).getAlias())) );
            //col.setColumnName(outerColName);
//            System.out.println(col.getColumnName());
//            System.out.println(col.getFullyQualifiedName());
//            System.out.println(col.getName(true));
//            System.out.println(col.getName(false));
//            orderByElements.add(sqp.getOrderByElements().get(0));
//            plainSelect.setOrderByElements(orderByElements);
        }*/


        // TODO assemble to new query
    }

    @Override
    public void visit(SetOperationList setOperationList) {
        System.out.println("set operation visit!");
        System.out.println(setOperationList);
        setOperationList.getSelects().stream().forEach(value -> value.accept(this));
        System.out.println(this.getWhereItem());

    }

    @Override
    public void visit(WithItem withItem) {
        System.out.println("with item visit!");
        System.out.println(withItem);
    }

    @Override
    public void visit(ValuesStatement valuesStatement) {
        System.out.println("values visit!");
        System.out.println(valuesStatement);
    }
}
