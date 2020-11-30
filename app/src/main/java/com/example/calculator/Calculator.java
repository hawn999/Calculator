package com.example.calculator;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.math.BigDecimal;
import java.sql.Array;
import java.util.ArrayList;

import java.util.*;
import java.util.regex.Pattern;
import java.lang.*;


public class Calculator extends AppCompatActivity {
    //输入框
    private EditText editText=null;
    //表达式
    private String str="",res="";
    private boolean isNewExpression=true;
    private int numberOfRightBrackets=0;
    //是否取过相反数
    private boolean haveNegetive=false;
    //记录之前最后取反的位置
    private int negativeLocation=-2;
    private String value="",beforeValue="";
    private int last_location=-1;//该位置后的为最后一个数，用于取相反数
    private String before="",after="";//位置为i字符的前后字符串
    private boolean canAddPoint=true;//能加小数点
    private boolean wrongExpression=false;
    private boolean wrongDivision=false;

    //对str进行运算
    private void cal(){
        String oldstr=str;//变换表达式之前用oldstr存储表达式
        calpercent();//计算百分号
        calsqrt();//计算根号
        //进行计算
        if (!wrongExpression) {
            addMul();//不全乘号
            //判断除法后是否为0
            judge();
            if (wrongDivision){
                Toast.makeText(Calculator.this,"除数不能为0，您输入的式子有误，请重新输入！",Toast.LENGTH_SHORT).show();
                str="";
                //重置初始条件
                haveNegetive=false;
                value="";
                beforeValue="";
                negativeLocation=-2;
                last_location=-1;
                before="";after="";
                canAddPoint=true;
                wrongExpression=false;
                wrongDivision=false;
                showEditText();
            }else {
                StringBuffer post = toPostFix(str);
                res = toValue(post);
                //去除整数的小数点及小数点后的数
                for (int i = 0; i < res.length(); i++) {
                    if (res.charAt(i) == '.') {//遇到小数点
                        int j = res.length() - 1;
                        for (; j > i; j--) {
                            if (res.charAt(j) != '0')
                                break;
                        }
                        if (i == j) {
                            res = res.substring(0, i);
                        }
                    }
                }
                str = oldstr + "=" + res;
                showEditText();//展示结果
            }
        }else{
            Toast.makeText(Calculator.this,"对负数不能开根号，您输入的式子有误，请重新输入！",Toast.LENGTH_SHORT).show();
            str="";
            //重置初始条件
            haveNegetive=false;
            value="";
            beforeValue="";
            negativeLocation=-2;
            last_location=-1;
            before="";after="";
            canAddPoint=true;
            wrongExpression=false;
            showEditText();
        }
    }
    //判断除法后是否为0
    private void judge(){
        String oldstr=str,newstr=str;
        int n=str.length();
        for(int i=0;i<n-1;i++){
            String t=str.substring(i,i+2);
            if (str.substring(i,i+2).equals("÷(")){
                int number=0;//括号的数量
                int j=i+1;
                for (;j<n;j++){
                    if (oldstr.charAt(j) == '(') {
                        number++;
                    } else if (oldstr.charAt(j) == ')') {
                        number--;
                    }
                    if(number==0&&(oldstr.charAt(j)=='+'||oldstr.charAt(j)=='-'||oldstr.charAt(j)=='×'||oldstr.charAt(j)=='÷'||oldstr.charAt(j)==')')){
                        break;
                    }
                }
                String expression=newstr.substring(i+1,j+1);//除法后的式子
                if (i+2<=n-1&&oldstr.charAt(i+2)=='-'){
                    expression="(0"+expression.substring(1,expression.length());
                }
                StringBuffer post=toPostFix(expression);
                double value=toDoubleValue(post);
                if (value==0){//除数为0
                    wrongDivision=true;
                    break;
                }
            }
            else if (str.substring(i,i+2).equals("÷√")){
                int number=0;//括号的数量
                int j=i+2;
                for (;j<n;j++) {
                    if (oldstr.charAt(j) == '(') {
                        number++;
                    } else if (oldstr.charAt(j) == ')') {
                        number--;
                    }
                    if (number == 0 && (oldstr.charAt(j) == '+' || oldstr.charAt(j) == '-' || oldstr.charAt(j) == '×' || oldstr.charAt(j) == '÷' || oldstr.charAt(j) == ')')) {
                        break;
                    }
                }
                String expression=newstr.substring(i+1,j+1);//除法后的式子
                if (i+2<=n-1&&oldstr.charAt(i+2)=='-'){
                    expression="(0"+expression.substring(1,expression.length());
                }
                StringBuffer post=toPostFix(expression);
                double value=toDoubleValue(post);
                if (value==0){//除数为0
                    wrongDivision=true;
                    break;
                }else {
                }
            }
        }
    }
    //补齐表达式中省略的乘号
    private void addMul(){
        String before="",after="",newstr="",oldstr="";
        //对括号进行判断
        int n=str.length();
        int numberOdAdd=0;//新str的变化量
        oldstr=str;
        newstr=str;
        for (int i=0;i<n-1;i++){//从第二个字符开始
            if (i>0) {
                //对newstr进行修改
                char beforet = newstr.charAt(i - 1+numberOdAdd);//i-1
                char aftert = newstr.charAt(i + 1+numberOdAdd);//i+1
                before = newstr.substring(0, i + numberOdAdd);//字符i前的string
                after = newstr.substring(i + numberOdAdd, newstr.length());//字符i后的string，包括i
                if (oldstr.charAt(i) == '(') {
                    if ((beforet == ')' || (beforet >= '0' && beforet <= '9'))&&aftert!='-') {
                        newstr = before + "×" + after.substring(0, 1) + "0+" + after.substring(1, after.length());
                        numberOdAdd += 3;
                    } else if (aftert == '-') {
                        if (beforet=='-'||beforet=='+'||beforet=='×'||beforet=='-'){
                            newstr = before + "(0" + after.substring(1, after.length());
                            numberOdAdd += 1;
                        }else {
                            newstr = before + "×(0" + after.substring(1, after.length());
                            numberOdAdd += 2;
                        }
                    }

                } else if (oldstr.charAt(i) == ')') {
                    if (aftert >= '0' && aftert <= '9') {
                        newstr = before + ")×" + after;
                        numberOdAdd += 2;
                    }
                }
            }else if (i==0){
                if (oldstr.charAt(i) == '(') {
                   if (oldstr.charAt(i+1)=='-'){
                       newstr="(0"+newstr.substring(1,newstr.length());
                       numberOdAdd=numberOdAdd+1;
                   }
                }
            }
        }
        if (str.charAt(0)=='-'){
            int i=0;
            for (;i<n;i++){
//                if (str.charAt(i)=='')
            }
            newstr="0"+newstr;
        }

        str=newstr;
    }

    //对str中的sqrt进行运算
    private void calsqrt(){
        int n=str.length();
        int numberOfAdd=0;//新str变化的位数
        String oldstr=str,newstr=str;
        for (int i=0;i<n-1;i++){
            if (oldstr.charAt(i)=='√'){
                int number=0;//括号的数量
                int j=i;
                for (;j<n;j++){
                    if (oldstr.charAt(i+1)=='(') {
                        if (oldstr.charAt(j) == '(') {
                            number++;
                        } else if (oldstr.charAt(j) == ')') {
                            number--;
                        }
                        if(number==0&&(oldstr.charAt(j)=='+'||oldstr.charAt(j)=='-'||oldstr.charAt(j)=='×'||oldstr.charAt(j)=='÷')){
                            break;
                        }
                    }else{
                        if(number==0&&(oldstr.charAt(j)=='+'||oldstr.charAt(j)=='-'||oldstr.charAt(j)=='×'||oldstr.charAt(j)=='÷'||oldstr.charAt(j)=='(')){
                            break;
                        }
                    }

                }

                String expression=newstr.substring(i+1+numberOfAdd,j+numberOfAdd);//要开根号的式子
                if (i+2<=n-1&&oldstr.charAt(i+2)=='-'){
                    expression="(0"+expression.substring(1,expression.length());
                }
                StringBuffer post=toPostFix(expression);
                double value=toDoubleValue(post);
                if (value<0){//根号下为负数
                    wrongExpression=true;
                    break;
                }else {
                    String valueString = String.valueOf(Math.sqrt(value));
                    if (i-1>=0&&oldstr.charAt(i-1)>='0'&&oldstr.charAt(i-1)<='9'){
                        newstr=newstr.substring(0, i + numberOfAdd) + "×"+valueString + newstr.substring(j + numberOfAdd, newstr.length());
                        numberOfAdd = numberOfAdd + valueString.length() - expression.length()+1;
                        str = newstr;
                    }else {
                        newstr = newstr.substring(0, i + numberOfAdd) + valueString + newstr.substring(j + numberOfAdd, newstr.length());
                        numberOfAdd = numberOfAdd + valueString.length() - expression.length();
                        str = newstr;
                    }
                }
            }
        }


    }
    //对str中的%转化成乘法的形式
    private void calpercent(){
        int n=str.length();
        String oldstr=str,newstr=str;
        int numberOfAdd=0;
        for (int i=1;i<n;i++){
            before=newstr.substring(0,i+numberOfAdd);//字符前的string
            after=newstr.substring(i+1+numberOfAdd,newstr.length());//字符后的string
            if (oldstr.charAt(i)=='%'){
                newstr=before+"×0.01"+after;
                numberOfAdd+=5;
            }
        }
        str=newstr;
    }
    //转化为后缀表达式
    private StringBuffer toPostFix(String infix){
        Stack<String> st=new Stack();
        StringBuffer postfix=new StringBuffer();
        for(int i=0;i<infix.length();i++){
            char ch=infix.charAt(i);
            switch(ch){
                case '(':
                    st.push(ch+"");
                    break;
                case '+':
                case '-':
                    while(!st.isEmpty()&&!"(".equals(st.peek())){
                        postfix.append(st.pop());
                    }
                    st.push(ch+"");
                    break;
                case '÷':
                case '×':
                    while(!st.isEmpty()&&(st.peek().equals("×")||st.peek().equals("÷"))){
                        postfix.append(st.pop());
                    }
                    st.push(ch+"");
                    break;
                case ')':
                    String temp=st.pop();
                    while(!st.isEmpty()&&!temp.equals("(")){
                        postfix.append(temp);
                        temp=st.pop();
                    }
                    break;
                default:
                    while(((ch>='0'&&ch<='9')||ch=='.')&&i<infix.length()){
                        postfix.append(ch);
                        i++;
                        if(i<infix.length())
                            ch=infix.charAt(i);
                    }
                    postfix.append(" ");
                    i--;
                    break;
            }
        }
        while(!st.isEmpty()){
            postfix.append(st.pop());
        }
        return postfix;
    }

    //使用BigDecimal计算后缀表达式
    private String toValue(StringBuffer postfix){
        Stack<Double> st=new Stack();
        String result="";
        double value=0;
        for(int i=0;i<postfix.length();i++){
            char ch=postfix.charAt(i);
            if((ch>='0'&&ch<='9')||String.valueOf(ch)=="."){
                String str="";
                while(ch!=' '){
                    str+=ch+"";
                    ch=postfix.charAt(++i);
                }
                st.push(Double.valueOf(str));
            }
            else{
                BigDecimal y=new BigDecimal(Double.toString(st.pop()));
                BigDecimal x=new BigDecimal(Double.toString(st.pop()));
                switch(ch){
                    case '+':
                        value=x.add(y).doubleValue();
                        break;
                    case '-':
                        value=x.subtract(y).doubleValue();
                        break;
                    case '×':
                        value=x.multiply(y).doubleValue();
                        break;
                    case '÷':
                        value=x.divide(y,10,BigDecimal.ROUND_HALF_UP).doubleValue();
                        break;
                }
                st.push(value);
            }
        }

        result=String.valueOf(st.pop());
        return result;
    }

    //使用BigDecimal计算后缀表达式
    private double toDoubleValue(StringBuffer postfix){
        Stack<Double> st=new Stack();
        String result="";
        double value=0;
        for(int i=0;i<postfix.length();i++){
            char ch=postfix.charAt(i);
            if((ch>='0'&&ch<='9')||String.valueOf(ch)=="."){
                String str="";
                while(ch!=' '){
                    str+=ch+"";
                    ch=postfix.charAt(++i);
                }
                st.push(Double.valueOf(str));
            }
            else{
                BigDecimal y=new BigDecimal(Double.toString(st.pop()));
                BigDecimal x=new BigDecimal(Double.toString(st.pop()));
                switch(ch){
                    case '+':
                        value=x.add(y).doubleValue();
                        break;
                    case '-':
                        value=x.subtract(y).doubleValue();
                        break;
                    case '×':
                        value=x.multiply(y).doubleValue();
                        break;
                    case '÷':
                        value=x.divide(y,10,BigDecimal.ROUND_HALF_UP).doubleValue();
                        break;
                }
                st.push(value);
            }
        }

        return st.pop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_calculator);


        editText=(EditText)findViewById(R.id.edit_text);
        Button clear=(Button)findViewById(R.id.clear);
        final Button negation=(Button)findViewById(R.id.negation);
        Button percent=(Button)findViewById(R.id.percent);
        Button div=(Button)findViewById(R.id.div);
        Button mul=(Button)findViewById(R.id.mul);
        Button sub=(Button)findViewById(R.id.sub);
        Button add=(Button)findViewById(R.id.add);
        Button equal=(Button)findViewById(R.id.equal);
        Button point=(Button)findViewById(R.id.point);
        Button del=(Button)findViewById(R.id.del);
        Button zero=(Button)findViewById(R.id.zero);
        Button one=(Button)findViewById(R.id.one);
        Button two=(Button)findViewById(R.id.two);
        Button three=(Button)findViewById(R.id.three);
        Button four=(Button)findViewById(R.id.four);
        Button five=(Button)findViewById(R.id.five);
        Button six=(Button)findViewById(R.id.six);
        Button seven=(Button)findViewById(R.id.seven);
        Button eight=(Button)findViewById(R.id.eight);
        Button nine=(Button)findViewById(R.id.nine);
        Button sqrt=(Button)findViewById(R.id.sqrt);
        Button RBrackets=(Button)findViewById(R.id.RBrackets);
        Button LBrackets=(Button)findViewById(R.id.LBrackets);

        zero.setOnClickListener(new numberButton("zero"));
        one.setOnClickListener(new numberButton("one"));
        two.setOnClickListener(new numberButton("two"));
        three.setOnClickListener(new numberButton("three"));
        four.setOnClickListener(new numberButton("four"));
        five.setOnClickListener(new numberButton("five"));
        six.setOnClickListener(new numberButton("six"));
        seven.setOnClickListener(new numberButton("seven"));
        eight.setOnClickListener(new numberButton("eight"));
        nine.setOnClickListener(new numberButton("nine"));

        div.setOnClickListener(new operatorButton("div"));
        mul.setOnClickListener(new operatorButton("mul"));
        add.setOnClickListener(new operatorButton("add"));
        sub.setOnClickListener(new operatorButton("sub"));
        //清空键监听器
        clear.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                str="";
                showEditText();
                //清空后是新的表达式
                isNewExpression=true;
                //清空后没有括号
                numberOfRightBrackets=0;
                haveNegetive=false;
                value="";
                beforeValue="";
                negativeLocation=-2;
                last_location=-1;
                before="";after="";

            }
        });
        //删除键监听器
        del.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                if (str.length()>0){
                    //若删除括号，要更新括号数
                    if (endAsRBracekts())
                        numberOfRightBrackets++;
                    if (endAsLBracekts())
                        numberOfRightBrackets--;
                    str=str.substring(0,str.length()-1);
                }
                if (str.length()==0){
                    isNewExpression=true;
                    haveNegetive=false;
                    value="";
                    beforeValue="";
                    negativeLocation=-2;
                    last_location=-1;
                    before="";after="";
                }

                showEditText();
            }
        });
        //根号监听器
        sqrt.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                if (isNewExpression) {
                    str="";//新式子
                    isNewExpression = false;
                    str = str + "√";
                    canAddPoint=true;
                }else if (endAsRBracekts()){
                    Toast.makeText(Calculator.this, "根号不能加在右括号后，请重新输入", Toast.LENGTH_SHORT).show();
                }else if(endAsPercent()){
                    Toast.makeText(Calculator.this, "根号不能加在百分号后，请重新输入", Toast.LENGTH_SHORT).show();
                }else if (endAsPoint()){
                    Toast.makeText(Calculator.this, "根号不能加在小数点后，请重新输入", Toast.LENGTH_SHORT).show();
                }else if(endAsSqrt()){
                    Toast.makeText(Calculator.this, "根号不能加在根号后，请重新输入", Toast.LENGTH_SHORT).show();
                }else{
                    str=str+"√";
                    canAddPoint=true;
                }
                showEditText();
            }
        });

        //左括号监听器
        RBrackets.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (isNewExpression){
                    str="";
                    isNewExpression=false;
                    str=str+"(";
                    numberOfRightBrackets++;
                    canAddPoint=true;
                    showEditText();
                }else if (!endAsPoint()&&!endAsPercent()){
                    str=str+"(";
                    numberOfRightBrackets++;
                    canAddPoint=true;
                    showEditText();
                }else{
                    Toast.makeText(Calculator.this,"左括号不能加在小数点和百分号后面，请重新输入",Toast.LENGTH_SHORT).show();
                }

            }
        });

        //右括号监听器
        LBrackets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNewExpression){
                    str="";
                    Toast.makeText(Calculator.this,"右括号不能加在表达式开头，请重新输入",Toast.LENGTH_SHORT).show();
                }else if(numberOfRightBrackets==0){
                    Toast.makeText(Calculator.this,"表达式前没有与之匹配的左括号，请重新输入",Toast.LENGTH_SHORT).show();
                }else if (numberOfRightBrackets>0){
                    if (endAsRBracekts()||endAsPercent()||isNumberEnd(str)){
                        numberOfRightBrackets--;
                        str=str+")";
                        canAddPoint=true;
                        showEditText();
                    }else{
                        Toast.makeText(Calculator.this,"右括号只能加在数字、右括号、百分号后面，请重新输入",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        //取反监听器
        negation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                str=str;
                //要取反的数，为str最后一位，到str中最后一个ArrayList中的字符之间的字符
                ArrayList<String> a=new ArrayList<String>();
                a.add("+");a.add("-");a.add("÷");a.add("×");
                a.add("(");a.add("√");
                if (isNewExpression){
                    Toast.makeText(Calculator.this,"请先输入一个数值，再取反",Toast.LENGTH_SHORT).show();
                }else if (endAsPercent()||isNumberEnd(str)){
                    if (str.length()>value.length()+beforeValue.length()+3){//对新的位置的数取反
                        haveNegetive=false;
                        value="";
                        beforeValue="";
                    }

                    if (haveNegetive==false){
                        for (int i=str.length()-1;i>=0;i--) {
                            if (a.contains(String.valueOf(str.charAt(i)))) {
                                last_location = i;
                                break;
                            }
                        }
                    }

                    if (negativeLocation==last_location){
                        str=beforeValue+value;
                        negativeLocation--;
                        haveNegetive=false;
                    }else{
                        value=str.substring(last_location+1,str.length());
                        beforeValue=str.substring(0,last_location+1);
                        str=beforeValue+"(-"+value+")";
                        haveNegetive=true;//标记取过相反数
                        negativeLocation=last_location;
                    }
                    showEditText();

                }else if(endAsRBracekts()){//对括号里的数取反
                    int number=0;
                    if (str.length()>value.length()+beforeValue.length()+3){//对新的位置的数取反
                        haveNegetive=false;
                        value="";
                        beforeValue="";
                    }

                    if (haveNegetive==false){
                        for (int i=str.length()-1;i>=0;i--) {
                            if (str.charAt(i)==')'){
                                number++;
                            }
                            else if (str.charAt(i)=='('){
                                number--;
                            }
                            if (str.charAt(i)=='('&&number==0) {
                                last_location = i;
                                break;
                            }
                        }
                    }

                    if (negativeLocation==last_location){
                        str=beforeValue+value;
                        negativeLocation--;
                        haveNegetive=false;
                    }else{
                        value=str.substring(last_location+1,str.length());
                        beforeValue=str.substring(0,last_location+1);
                        str=beforeValue+"-("+value+")";

                        haveNegetive=true;//标记取过相反数
                        negativeLocation=last_location;
                    }
                    showEditText();
                }
                else{
                    Toast.makeText(Calculator.this,"前方没有正确格式的数值，不能取反!",Toast.LENGTH_SHORT).show();
                }
                str=str;
            }
        });

        //百分比监听器
        percent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNewExpression){
                    str="";
                    Toast.makeText(Calculator.this,"百分号不能加在表达式开头，请重新输入",Toast.LENGTH_SHORT).show();
                }else if (endAsRBracekts()||isNumberEnd(str)){
                    str=str+"%";
                    showEditText();
                }else{
                    Toast.makeText(Calculator.this,"百分号只能能加在数字、右括号后面，请重新输入",Toast.LENGTH_SHORT).show();
                }
            }
        });

        //小数点监听器
        point.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNewExpression){
                    str="";
                    Toast.makeText(Calculator.this,"小数点不能作为表达式的开头，请重新输入！",Toast.LENGTH_SHORT).show();
                }else if (!isNumberEnd(str)){
                    Toast.makeText(Calculator.this,"小数点必须加在数字后面，请重新输入！",Toast.LENGTH_SHORT).show();
                }else if(!canAddPoint){
                    Toast.makeText(Calculator.this,"一个数中无法添加多个小数点，请重新输入！",Toast.LENGTH_SHORT).show();
                }else{
                    str=str+".";
                    showEditText();
                    canAddPoint=false;//不可以再加小数点了，直到新的数值
                }
            }
        });

        //等于号监听器
        equal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (numberOfRightBrackets==0&&(isNumberEnd(str)||endAsRBracekts()||endAsPercent())&&isNewExpression==false){
                    cal();
                    isNewExpression=true;
                    //重置初始条件
                    haveNegetive=false;
                    value="";
                    beforeValue="";
                    negativeLocation=-2;
                    last_location=-1;
                    before="";after="";
                    canAddPoint=true;
                    wrongExpression=false;
                }else if (isNewExpression){
                    str="";
                    //重置初始条件
                    haveNegetive=false;
                    value="";
                    beforeValue="";
                    negativeLocation=-2;
                    last_location=-1;
                    before="";after="";
                    canAddPoint=true;
                    wrongExpression=false;
                    Toast.makeText(Calculator.this,"请输入新表达式！",Toast.LENGTH_SHORT).show();
                    showEditText();
                }
                else{
                    Toast.makeText(Calculator.this,"请输入准确、完整的表达式！",Toast.LENGTH_SHORT).show();
                }
            }
        });
        //禁止通过键盘输入表达式
        editText.setKeyListener(null);
    }

    //更新EditText显示内容
    private void showEditText(){
        StringBuilder builder=new StringBuilder();
        //显示内容
        editText.setText(str);
        editText.setSelection(str.length());

    }

    //是否以运算符结尾
    private boolean isOperatorEnd(){
        String pattern=".*[+-/×/÷]";
        if (Pattern.matches(pattern,str)){
            return true;
        }
        else {
            return false;
        }
    }
    //是否以数字结尾
    private boolean isNumberEnd(String string){
        String pattern=".*[0-9]";
        if (Pattern.matches(pattern,string)){
            return true;
        }
        else {
            return false;
        }
    }
    //是否以右括号结尾
    private boolean endAsRBracekts(){
        String pattern=".*[)]";
        if (Pattern.matches(pattern,str)){
            return true;
        }
        else {
            return false;
        }
    }
    //是否以左括号结尾
    private boolean endAsLBracekts(){
        String pattern=".*[(]";
        if (Pattern.matches(pattern,str)){
            return true;
        }
        else {
            return false;
        }
    }
    //是否以根号号结尾
    private boolean endAsSqrt(){
        String pattern=".*[√]";
        if (Pattern.matches(pattern,str)){
            return true;
        }
        else {
            return false;
        }
    }
    //是否以百分号结尾
    private boolean endAsPercent(){
        String pattern=".*[%]";
        if (Pattern.matches(pattern,str)){
            return true;
        }
        else {
            return false;
        }
    }
    //是否以小数点结尾
    private boolean endAsPoint(){
        String pattern=".*[.]";
        if (Pattern.matches(pattern,str)){
            return true;
        }
        else {
            return false;
        }
    }
    //是否以除法结尾
    private boolean endAsDiv(){
        String pattern=".*[÷]";
        if (Pattern.matches(pattern,str)){
            return true;
        }
        else {
            return false;
        }
    }


    //数字按钮监听器
    class numberButton implements View.OnClickListener {
        String string_number;
        public numberButton(String number){
            switch (number){
                case "zero":
                    string_number="0";
                    break;
                case "one":
                    string_number="1";
                    break;
                case "two":
                    string_number="2";
                    break;
                case "three":
                    string_number="3";
                    break;
                case "four":
                    string_number="4";
                    break;
                case "five":
                    string_number="5";
                    break;
                case "six":
                    string_number="6";
                    break;
                case "seven":
                    string_number="7";
                    break;
                case "eight":
                    string_number="8";
                    break;
                case "nine":
                    string_number="9";
                    break;
            }

        }
        public void onClick(View view) {
            //新式子，清空str
            if (isNewExpression){
                str="";
                isNewExpression=false;
                str = str + string_number;
                showEditText();
            }else if (endAsPercent()){
                Toast.makeText(Calculator.this,"数字不能加在百分号后，请重新输入",Toast.LENGTH_SHORT).show();
            }else if (endAsRBracekts()){
                Toast.makeText(Calculator.this,"数字不能加在右括号后，请重新输入",Toast.LENGTH_SHORT).show();
            }else{
                isNewExpression=false;
                if (string_number=="0"){
                    if (endAsDiv()){
                        Toast.makeText(Calculator.this,"0不能加在除号后，请重新输入",Toast.LENGTH_SHORT).show();
                    }else{
                        str = str + string_number;
                        showEditText();
                    }
                }else{
                    str = str + string_number;
                    showEditText();
                }

            }

        }
    }

    //运算符按钮监听器
    class operatorButton implements View.OnClickListener{
        String string_operator="";
        public operatorButton(String operator){
            switch (operator){
                case "div":
                    string_operator="÷";
                    break;
                case "mul":
                    string_operator="×";
                    break;
                case "sub":
                    string_operator="-";
                    break;
                case "add":
                    string_operator="+";
                    break;
            }
        }
        public void onClick(View view){
            if (isNewExpression&&str.length()==0) {
                str = "";
                Toast.makeText(Calculator.this, "不能以运算符开头,请重新输入", Toast.LENGTH_SHORT).show();
            }else if(isOperatorEnd()){
                Toast.makeText(Calculator.this,"不能连续输入两个运算符，请重新输入",Toast.LENGTH_SHORT).show();
            }else if(endAsLBracekts()){
                Toast.makeText(Calculator.this,"运算符不能加在左括号后，请重新输入",Toast.LENGTH_SHORT).show();
            }else if(endAsSqrt()){
                Toast.makeText(Calculator.this,"运算符不能加在根号后，请重新输入",Toast.LENGTH_SHORT).show();
            }else if(endAsLBracekts()){
                Toast.makeText(Calculator.this,"运算符不能加在左括号后，请重新输入",Toast.LENGTH_SHORT).show();
            }else if(endAsPoint()){
                Toast.makeText(Calculator.this,"运算符不能加在小数点后，请重新输入",Toast.LENGTH_SHORT).show();
            }else if (isNewExpression&&res!=""){
                str=res+string_operator;//计算完，如果按符号键则继续运算
                isNewExpression=false;
            }else {
                isNewExpression = false;
                str=str+string_operator;
                canAddPoint=true;//能加小数点了
            }
            showEditText();
        }
    }

    //
}

