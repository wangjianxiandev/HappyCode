//角色向上走
Blockly.JavaScript['bx_move_up'] = function(block) {
    return 'move_up();\n';
};
//角色向左走
Blockly.JavaScript['bx_move_left'] = function(block) {
    return 'move_left();\n';
};
//角色向右走
Blockly.JavaScript['bx_move_right'] = function(block) {
    return 'move_right();\n';
};
//角色向下走
Blockly.JavaScript['bx_move_down'] = function(block) {
    return 'move_down();\n';
};

//重复执行n次
Blockly.JavaScript['bx_repeat'] = function(block) {
    var strTimes = block.getFieldValue("TIMES");
    var times = 0;
    //进行字符串到数字的转化
    times = parseInt(strTimes);
    if(isNaN(times)){
        times = 0;
    }
    var contentStatement = Blockly.JavaScript.statementToCode(block, 'CONTENT').substring(2);
    var code = 'for(var i = 0; i < '+times+'; i++){\n\t'+contentStatement+'}\n';
    return code;
};
