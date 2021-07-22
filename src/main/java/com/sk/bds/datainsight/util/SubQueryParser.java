package com.sk.bds.datainsight.util;


import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.values.ValuesStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class SubQueryParser implements SelectVisitor, FromItemVisitor {

    private List<SelectItem> selectItems = new ArrayList<>();
    private List<OrderByElement> orderByElements = new ArrayList<>();

    public List<SelectItem> getSelectItems() {
        return selectItems;
    }

    public List<OrderByElement> getOrderByElements() {
        return orderByElements;
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        System.out.println("(sub) plain select visit!");
        System.out.println(plainSelect);
        System.out.println("(sub) select items =");
        Map<String, String> selectItems = new TreeMap<>();
        List<String> selectItem = plainSelect.getSelectItems()
                .stream()
                .map(SelectItem::toString)
                .collect(Collectors.toList());
        for (String value : selectItem) {
            if (value.contains("AS")) {
                String[] list = value.trim().replace("AS", "/").trim().split("/");
                selectItems.put(list[0].trim(), list[1].replace("'", "").trim());
            }
        }
        boolean isExist = plainSelect.getOrderByElements() != null && !plainSelect.getOrderByElements().isEmpty();
        System.out.println("(sub) exist = " + isExist);
        if (!isExist) return;

        System.out.println("(sub) order by = ");
        plainSelect.getOrderByElements().forEach((OrderByElement e) -> {
            System.out.println(e);
            if (selectItems.containsKey(e.toString().trim())) {
                OrderByElement orderByElement = new OrderByElement();
                orderByElement.setExpression(new Column(selectItems.get(e.toString())));
                getOrderByElements().add(orderByElement);
            } else {
                getOrderByElements().add(e);
            }
        });

        // remove inner order by clause
        plainSelect.setOrderByElements(null);
    }

    @Override
    public void visit(SetOperationList setOperationList) {
        System.out.println("set operation visit!");
        System.out.println(setOperationList);
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

    @Override
    public void visit(Table table) {
        System.out.println("table visit!");
    }

    @Override
    public void visit(SubSelect subSelect) {
        System.out.println("subSelect visit!");
        subSelect.getSelectBody().accept(this); // go to line:8
    }

    @Override
    public void visit(SubJoin subJoin) {
        System.out.println("subJoin visit!");
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {
        System.out.println("lateralSubSelect visit!");
    }

    @Override
    public void visit(ValuesList valuesList) {
        System.out.println("valuesList visit!");
    }

    @Override
    public void visit(TableFunction tableFunction) {
        System.out.println("tableFunction visit!");
    }

    @Override
    public void visit(ParenthesisFromItem parenthesisFromItem) {
        System.out.println("parenthesisFromItem visit!");
    }


}
