package com.sk.bds.datainsight.util;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SubSelect;

public class WhereParser implements ExpressionVisitor {

    private Expression expression;
    private PlainSelect plainSelect;

    public WhereParser() {
    }

    public WhereParser(Expression expression, PlainSelect plainSelect) {
        this.expression = expression;
        this.plainSelect = plainSelect;
    }

    private Expression getExpression() {
        return expression;
    }

    @Override
    public void visit(BitwiseRightShift bitwiseRightShift) {
        System.out.println(bitwiseRightShift);
        System.out.println("bitwiseRightShift");
    }

    @Override
    public void visit(BitwiseLeftShift bitwiseLeftShift) {
        System.out.println(bitwiseLeftShift);
        System.out.println("bitwiseLeftShift");

    }

    @Override
    public void visit(NullValue nullValue) {
        System.out.println(nullValue);
        System.out.println("nullValue");
    }

    @Override
    public void visit(Function function) {
        System.out.println(function);
        System.out.println("function");

    }

    @Override
    public void visit(SignedExpression signedExpression) {
        System.out.println(signedExpression);
        System.out.println("signedExpression");
    }

    @Override
    public void visit(JdbcParameter jdbcParameter) {
        System.out.println(jdbcParameter);
        System.out.println("jdbcParameter");

    }

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {
        System.out.println(jdbcNamedParameter);
        System.out.println("jdbcNamedParameter");
    }

    @Override
    public void visit(DoubleValue doubleValue) {
        System.out.println(doubleValue);
        System.out.println("doubleValue");
    }

    @Override
    public void visit(LongValue longValue) {
        System.out.println(longValue);
    }

    @Override
    public void visit(HexValue hexValue) {
        System.out.println(hexValue);
        System.out.println("hexValue");
    }

    @Override
    public void visit(DateValue dateValue) {
        System.out.println(dateValue);
        System.out.println("dateValue");
    }

    @Override
    public void visit(TimeValue timeValue) {
        System.out.println(timeValue);
        System.out.println("timeValue");
    }

    @Override
    public void visit(TimestampValue timestampValue) {
        System.out.println(timestampValue);
        System.out.println("timestampValue");
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        System.out.println(parenthesis);
        System.out.println("parenthesis");
    }

    @Override
    public void visit(StringValue stringValue) {
        System.out.println(stringValue);
        System.out.println("stringValue");
    }

    @Override
    public void visit(Addition addition) {
        System.out.println(addition);
    }

    @Override
    public void visit(Division division) {
        System.out.println(division);
        System.out.println("division");
    }

    @Override
    public void visit(IntegerDivision integerDivision) {
        System.out.println(integerDivision);
        System.out.println("integerDivision");
    }

    @Override
    public void visit(Multiplication multiplication) {
        System.out.println(multiplication);
        System.out.println("multiplication");
    }

    @Override
    public void visit(Subtraction subtraction) {
        System.out.println(subtraction);
        System.out.println("subtraction");
    }

    @Override
    public void visit(AndExpression andExpression) {
        System.out.println(andExpression);
        AndExpression tempAndExpression = new AndExpression(andExpression, this.getExpression());
        plainSelect.setWhere(tempAndExpression);
        System.out.println("andExpression");

    }

    @Override
    public void visit(OrExpression orExpression) {
        System.out.println(orExpression);
        AndExpression andExpression = new AndExpression(orExpression, this.getExpression());
        plainSelect.setWhere(andExpression);
        System.out.println("orExpression");
    }

    @Override
    public void visit(Between between) {
        System.out.println(between);
        System.out.println("between");
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        System.out.println(equalsTo);
        AndExpression andExpression = new AndExpression(equalsTo, this.getExpression());
        plainSelect.setWhere(andExpression);
        System.out.println("equalsTo");
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        System.out.println(greaterThan);
        System.out.println("greaterThan");
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        System.out.println(greaterThanEquals);
        System.out.println("greaterThanEquals");
    }

    @Override
    public void visit(InExpression inExpression) {
        System.out.println(inExpression);
        System.out.println("inExpression");
    }

    @Override
    public void visit(FullTextSearch fullTextSearch) {
        System.out.println(fullTextSearch);
        System.out.println("fullTextSearch");
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        System.out.println(isNullExpression);
        System.out.println("isNullExpression");
    }

    @Override
    public void visit(IsBooleanExpression isBooleanExpression) {
        System.out.println(isBooleanExpression);
        System.out.println("isBooleanExpression");
    }

    @Override
    public void visit(LikeExpression likeExpression) {
        System.out.println(likeExpression);
        System.out.println("likeExpression");
    }

    @Override
    public void visit(MinorThan minorThan) {
        System.out.println(minorThan);
        AndExpression andExpression = new AndExpression(minorThan, this.getExpression());
        plainSelect.setWhere(andExpression);
        System.out.println("minorThan");
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        System.out.println(minorThanEquals);
        System.out.println("minorThanEquals");
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        System.out.println(notEqualsTo);
        System.out.println("notEqualsTo");
    }

    @Override
    public void visit(Column column) {
        System.out.println(column);
        System.out.println("column");
    }

    @Override
    public void visit(SubSelect subSelect) {
        System.out.println(subSelect);
        System.out.println("subSelect");
    }

    @Override
    public void visit(CaseExpression caseExpression) {
        System.out.println(caseExpression);
        System.out.println("caseExpression");
    }

    @Override
    public void visit(WhenClause whenClause) {
        System.out.println(whenClause);
        System.out.println("whenClause");
    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        System.out.println(existsExpression);
        System.out.println("existsExpression");
    }

    @Override
    public void visit(AllComparisonExpression allComparisonExpression) {
        System.out.println(allComparisonExpression);
        System.out.println("allComparisonExpression");
    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {
        System.out.println(anyComparisonExpression);
        System.out.println("anyComparisonExpression");
    }

    @Override
    public void visit(Concat concat) {
        System.out.println(concat);
        System.out.println("concat");
    }

    @Override
    public void visit(Matches matches) {
        System.out.println(matches);
        System.out.println("matches");
    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd) {
        System.out.println(bitwiseAnd);
        System.out.println("bitwiseAnd");
    }

    @Override
    public void visit(BitwiseOr bitwiseOr) {
        System.out.println(bitwiseOr);
        System.out.println("bitwiseOr");
    }

    @Override
    public void visit(BitwiseXor bitwiseXor) {
        System.out.println("bitwiseXor");
    }

    @Override
    public void visit(CastExpression castExpression) {
        System.out.println("castExpression");
    }

    @Override
    public void visit(Modulo modulo) {
        System.out.println("modulo");
    }

    @Override
    public void visit(AnalyticExpression analyticExpression) {
        System.out.println("analyticExpression");
    }

    @Override
    public void visit(ExtractExpression extractExpression) {
        System.out.println("extractExpression");
    }

    @Override
    public void visit(IntervalExpression intervalExpression) {
        System.out.println("intervalExpression");
    }

    @Override
    public void visit(OracleHierarchicalExpression oracleHierarchicalExpression) {

    }

    @Override
    public void visit(RegExpMatchOperator regExpMatchOperator) {

    }

    @Override
    public void visit(JsonExpression jsonExpression) {

    }

    @Override
    public void visit(JsonOperator jsonOperator) {

    }

    @Override
    public void visit(RegExpMySQLOperator regExpMySQLOperator) {

    }

    @Override
    public void visit(UserVariable userVariable) {

    }

    @Override
    public void visit(NumericBind numericBind) {

    }

    @Override
    public void visit(KeepExpression keepExpression) {

    }

    @Override
    public void visit(MySQLGroupConcat mySQLGroupConcat) {

    }

    @Override
    public void visit(ValueListExpression valueListExpression) {
        System.out.println("valueListExpression");
    }

    @Override
    public void visit(RowConstructor rowConstructor) {

    }

    @Override
    public void visit(OracleHint oracleHint) {

    }

    @Override
    public void visit(TimeKeyExpression timeKeyExpression) {

    }

    @Override
    public void visit(DateTimeLiteralExpression dateTimeLiteralExpression) {

    }

    @Override
    public void visit(NotExpression notExpression) {
        System.out.println("notExpression");
    }

    @Override
    public void visit(NextValExpression nextValExpression) {
        System.out.println(nextValExpression);
        System.out.println("nextValExpression");
    }

    @Override
    public void visit(CollateExpression collateExpression) {
        System.out.println(collateExpression);
        System.out.println("collateExpression");
    }

    @Override
    public void visit(SimilarToExpression similarToExpression) {
        System.out.println(similarToExpression);
        System.out.println("similarToExpression");
    }

    @Override
    public void visit(ArrayExpression arrayExpression) {
        System.out.println(arrayExpression);
        System.out.println("arrayExpression");
    }
}
