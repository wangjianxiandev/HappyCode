'use strict';

var Box = function () {
    //在代码中用到的一些常量
    const TYPE_BLOCK = -1;   //-1表示不能走的障碍
    const TYPE_PATH = 0;     //0 表示可以走的通道
    const TYPE_CURRENT = 1;  //1 表示角色当前位置
    const TYPE_BOX = 2;      //2 表示箱子
    const TYPE_BOMB = 3;     //3 表示炸弹
    const TYPE_COVER_BOX = 4;//4 表示箱子覆盖在炸弹上
    const TYPE_COVER_PY = 5; //5 表示人覆盖在炸弹上

    //游戏数据矩阵，初始值都为-1，在init方法中接收外界的赋值
    var gameData = [
        [-1, -1, -1, -1, -1, -1, -1, -1, -1, -1],
        [-1, -1, -1, -1, -1, -1, -1, -1, -1, -1],
        [-1, -1, -1, -1, -1, -1, -1, -1, -1, -1],
        [-1, -1, -1, -1, -1, -1, -1, -1, -1, -1],
        [-1, -1, -1, -1, -1, -1, -1, -1, -1, -1],
        [-1, -1, -1, -1, -1, -1, -1, -1, -1, -1],
        [-1, -1, -1, -1, -1, -1, -1, -1, -1, -1],
        [-1, -1, -1, -1, -1, -1, -1, -1, -1, -1],
        [-1, -1, -1, -1, -1, -1, -1, -1, -1, -1],
        [-1, -1, -1, -1, -1, -1, -1, -1, -1, -1]
    ];

    //是否成功
    var flag_success = false;

    //所用时间,根据所用时间来进行星级评定
    var usedTime = 0;
    //游戏初始化时的时间
    var initTime = 0;
    //检查游戏是否结束时的时间
    var finishTime = 0;

    //获取屏幕的宽度和高度
    var screen_width = window.innerWidth;
    var screen_height = window.innerHeight;
    //游戏区域的尺寸
    var game_div_size = Math.min(screen_width, screen_height) - 20;
    var single_div_size = game_div_size / 10;

    //初始化游戏界面。参数data和direction分别代表游戏开始时的界面元素和小蝌蚪朝向
    var init = function (data) {
        //为initTime赋值
        initTime = new Date().getTime();

        //根据屏幕尺寸，动态设置游戏区域的宽和高
        var gameDivs = document.getElementById("game");
/*        gameDivs.children().remove();
*/        gameDivs.style.width = game_div_size + 'px';
        gameDivs.style.height = game_div_size + 'px';
        //设置gameDiv的位置
        var top = Math.max(10, (screen_height - game_div_size) / 2);
        var left = Math.max(10, (screen_width - game_div_size) / 2);
        gameDivs.style.top = top + 'px';
        gameDivs.style.left = left + 'px';
        for (var i = 0; i < data.length; i++) {
            for (var j = 0; j < data[0].length; j++) {
                gameData[i][j] = data[i][j];
                var newNode = document.createElement('div');
                //设置宽和高
                newNode.style.height = single_div_size + 'px';
                newNode.style.width = single_div_size + 'px';
                //设置位置
                newNode.style.top = (i * single_div_size) + 'px';
                newNode.style.left = (j * single_div_size) + 'px';
                //设置背景图片的缩放尺寸
                newNode.style.backgroundSize = single_div_size + 'px';

                /*设置每个元素的className*/
                //是不可以走的block
                if (data[i][j] == TYPE_BLOCK) {
                    //class=block代表该元素是障碍物，class=line+i表示该元素的所在行，class=col+j表示该元素所在列（便于定位）
                    newNode.className = 'block' + ' ' + 'line' + i + ' ' + 'col' + j;
                }
                //是可以走的path
                else if (data[i][j] === TYPE_PATH) {
                    newNode.className = 'path' + ' ' + 'line' + i + ' ' + 'col' + j;
                }
                //是箱子
                else if (data[i][j] == TYPE_BOX) {
                    newNode.className= 'box' + ' '+ 'line' + i + ' ' + 'col' + j;
                }
                //是炸弹
                else if( data[i][j] == TYPE_BOMB) {
                    newNode.className= 'bomb' + ' '+ 'line' + i + ' ' + 'col' + j;
                }
                //是被箱子覆盖的炸弹
                else if( data[i][j] == TYPE_COVER_BOX) {
                    newNode.className= 'cover_box' + ' '+ 'line' + i + ' ' + 'col' + j;
                }
                //是被人覆盖的炸弹
                else if( data[i][j] == TYPE_COVER_PY) {
                    newNode.className= 'cover_py' + ' '+ 'line' + i + ' ' + 'col' + j;
                }
                //人物的当前所在位置current
                else if (data[i][j] == TYPE_CURRENT) {
                    //小蝌蚪的class固定为current，其背景图片根据mDirection的值动态加载
                    newNode.className = 'current' + ' ' + 'line' + i + ' ' + 'col' + j;
                    //根据角色不同的方向加载不同的背景图片
                
                }
                //向gameDiv中添加当前元素
                gameDivs.appendChild(newNode);
            }
        }
    };

    //根据当前的方向向前走，也可以表示向上下左右走（1：上，2：右，3：下，4：左）
    var move_up = function () {
        var index_i = -1;
        var index_j = -1;
        //遍历这个数据矩阵，找到当前的位置
        for (var i = 0; i < gameData.length; i++) {
            for (var j = 0; j < gameData[0].length; j++) {
                //若当前已经到达终点，则gameData对应值为TYPE_SUCCESS，否则为TYPE_CURRENT
                if (gameData[i][j] == TYPE_CURRENT || gameData[i][j] == TYPE_COVER_PY) {
                    index_i = i;
                    index_j = j;
                    break;
                }
            }
        }

        if(gameData[index_i - 1][ index_j] != TYPE_BLOCK){
            // is path
            if(gameData[index_i - 1][index_j] == TYPE_PATH){
                if(gameData[index_i][index_j] == TYPE_COVER_PY){
                    gameData[index_i][index_j] = TYPE_BOMB;
                }else{
                    gameData[index_i][index_j] = TYPE_PATH;
                }
                gameData[index_i - 1][index_j] = TYPE_CURRENT;
//                $('.current').animate({top: '-=' + single_div_size + 'px'}, 1000);//向前游动的动画
            }
            else if(gameData[index_i - 1][index_j] == TYPE_BOMB){
                if(gameData[index_i][index_j] == TYPE_COVER_PY){
                    gameData[index_i][index_j] = TYPE_BOMB;
                }else{
                    gameData[index_i][index_j] = TYPE_PATH;
                }
                gameData[index_i - 1][index_j] = TYPE_COVER_PY;
//                $('.current').animate({top: '-=' + single_div_size + 'px'}, 1000);//向前游动的动画
            }
            else if(gameData[index_i - 1][index_j] == TYPE_COVER_BOX){
                if(gameData[index_i - 2][index_j] == TYPE_BOMB){
                    if(gameData[index_i][index_j] == TYPE_COVER_PY){
                        gameData[index_i][index_j] = TYPE_BOMB;
                    }else{
                        gameData[index_i][index_j] = TYPE_PATH;
                    }
                    gameData[index_i - 1][index_j] = TYPE_COVER_PY;
                    gameData[index_i - 2][index_j] = TYPE_COVER_BOX;
                }
                else if(gameData[index_i - 2][index_j] == TYPE_PATH){
                    if(gameData[index_i][index_j] == TYPE_COVER_PY){
                        gameData[index_i][index_j] = TYPE_BOMB;
                    }else{
                        gameData[index_i][index_j] = TYPE_PATH;
                    }
                    gameData[index_i - 1][index_j] = TYPE_COVER_PY;
                    gameData[index_i - 2][index_j] = TYPE_BOX;
                }
                else{
                    //
                }
            }
            else if(gameData[index_i - 1][index_j] == TYPE_BOX){
                if(gameData[index_i - 2][index_j] == TYPE_BOMB){
                    if(gameData[index_i][index_j] == TYPE_COVER_PY){
                        gameData[index_i][index_j] = TYPE_BOMB;
                    }else{
                        gameData[index_i][index_j] = TYPE_PATH;
                    }
                    gameData[index_i - 1][index_j] = TYPE_CURRENT;
                    gameData[index_i - 2][index_j] = TYPE_COVER_BOX;
                }
                else if(gameData[index_i - 2][index_j] == TYPE_PATH){
                    if(gameData[index_i][index_j] == TYPE_COVER_PY){
                        gameData[index_i][index_j] = TYPE_BOMB;
                    }else{
                        gameData[index_i][index_j] = TYPE_PATH;
                    }
                    gameData[index_i - 1][index_j] = TYPE_CURRENT;
                    gameData[index_i - 2][index_j] = TYPE_BOX;
                }
                else{
                    //
                }
            }
        }
        else{
            //无法移动
        }
        init(gameData);
    };

    var move_down = function () {
            var index_i = -1;
            var index_j = -1;
            //遍历这个数据矩阵，找到当前的位置
            for (var i = 0; i < gameData.length; i++) {
                for (var j = 0; j < gameData[0].length; j++) {
                    //若当前已经到达终点，则gameData对应值为TYPE_SUCCESS，否则为TYPE_CURRENT
                    if (gameData[i][j] == TYPE_CURRENT || gameData[i][j] == TYPE_COVER_PY) {
                        index_i = i;
                        index_j = j;
                        break;
                    }
                }
            }

            if(gameData[index_i + 1][ index_j] != TYPE_BLOCK){
                // is path
                if(gameData[index_i + 1][index_j] == TYPE_PATH){
                    if(gameData[index_i][index_j] == TYPE_COVER_PY){
                        gameData[index_i][index_j] = TYPE_BOMB;
                    }else{
                        gameData[index_i][index_j] = TYPE_PATH;
                    }
                    gameData[index_i + 1][index_j] = TYPE_CURRENT;
    //                $('.current').animate({top: '-=' + single_div_size + 'px'}, 1000);//向前游动的动画
                }
                else if(gameData[index_i + 1][index_j] == TYPE_BOMB){
                    if(gameData[index_i][index_j] == TYPE_COVER_PY){
                        gameData[index_i][index_j] = TYPE_BOMB;
                    }else{
                        gameData[index_i][index_j] = TYPE_PATH;
                    }
                    gameData[index_i + 1][index_j] = TYPE_COVER_PY;
    //                $('.current').animate({top: '-=' + single_div_size + 'px'}, 1000);//向前游动的动画
                }
                else if(gameData[index_i + 1][index_j] == TYPE_COVER_BOX){
                    if(gameData[index_i + 2][index_j] == TYPE_BOMB){
                        if(gameData[index_i][index_j] == TYPE_COVER_PY){
                            gameData[index_i][index_j] = TYPE_BOMB;
                        }else{
                            gameData[index_i][index_j] = TYPE_PATH;
                        }
                        gameData[index_i + 1][index_j] = TYPE_COVER_PY;
                        gameData[index_i + 2][index_j] = TYPE_COVER_BOX;
                    }
                    else if(gameData[index_i + 2][index_j] == TYPE_PATH){
                        if(gameData[index_i][index_j] == TYPE_COVER_PY){
                            gameData[index_i][index_j] = TYPE_BOMB;
                        }else{
                            gameData[index_i][index_j] = TYPE_PATH;
                        }
                        gameData[index_i + 1][index_j] = TYPE_COVER_PY;
                        gameData[index_i + 2][index_j] = TYPE_BOX;
                    }
                    else{
                        //
                    }
                }
                else if(gameData[index_i + 1][index_j] == TYPE_BOX){
                    if(gameData[index_i + 2][index_j] == TYPE_BOMB){
                        if(gameData[index_i][index_j] == TYPE_COVER_PY){
                            gameData[index_i][index_j] = TYPE_BOMB;
                        }else{
                            gameData[index_i][index_j] = TYPE_PATH;
                        }
                        gameData[index_i + 1][index_j] = TYPE_CURRENT;
                        gameData[index_i + 2][index_j] = TYPE_COVER_BOX;
                    }
                    else if(gameData[index_i + 2][index_j] == TYPE_PATH){
                        if(gameData[index_i][index_j] == TYPE_COVER_PY){
                            gameData[index_i][index_j] = TYPE_BOMB;
                        }else{
                            gameData[index_i][index_j] = TYPE_PATH;
                        }
                        gameData[index_i + 1][index_j] = TYPE_CURRENT;
                        gameData[index_i + 2][index_j] = TYPE_BOX;
                    }
                    else{
                        //
                    }
                }
            }
            else{
                //无法移动
            }
            init(gameData);
        };

    var move_left = function () {
            var index_i = -1;
            var index_j = -1;
            //遍历这个数据矩阵，找到当前的位置
            for (var i = 0; i < gameData.length; i++) {
                for (var j = 0; j < gameData[0].length; j++) {
                    //若当前已经到达终点，则gameData对应值为TYPE_SUCCESS，否则为TYPE_CURRENT
                    if (gameData[i][j] == TYPE_CURRENT || gameData[i][j] == TYPE_COVER_PY) {
                        index_i = i;
                        index_j = j;
                        break;
                    }
                }
            }

            if(gameData[index_i ][ index_j - 1] != TYPE_BLOCK){
                // is path
                if(gameData[index_i ][index_j -1] == TYPE_PATH){
                    if(gameData[index_i][index_j] == TYPE_COVER_PY){
                        gameData[index_i][index_j] = TYPE_BOMB;
                    }else{
                        gameData[index_i][index_j] = TYPE_PATH;
                    }
                    gameData[index_i ][index_j -1] = TYPE_CURRENT;
    //                $('.current').animate({top: '-=' + single_div_size + 'px'}, 1000);//向前游动的动画
                }
                else if(gameData[index_i ][index_j -1] == TYPE_BOMB){
                    if(gameData[index_i][index_j] == TYPE_COVER_PY){
                        gameData[index_i][index_j] = TYPE_BOMB;
                    }else{
                        gameData[index_i][index_j] = TYPE_PATH;
                    }
                    gameData[index_i ][index_j -1] = TYPE_COVER_PY;
    //                $('.current').animate({top: '-=' + single_div_size + 'px'}, 1000);//向前游动的动画
                }
                else if(gameData[index_i ][index_j -1] == TYPE_COVER_BOX){
                    if(gameData[index_i ][index_j -2] == TYPE_BOMB){
                        if(gameData[index_i][index_j] == TYPE_COVER_PY){
                            gameData[index_i][index_j] = TYPE_BOMB;
                        }else{
                            gameData[index_i][index_j] = TYPE_PATH;
                        }
                        gameData[index_i ][index_j -1] = TYPE_COVER_PY;
                        gameData[index_i ][index_j -2] = TYPE_COVER_BOX;
                    }
                    else if(gameData[index_i ][index_j -2] == TYPE_PATH){
                        if(gameData[index_i][index_j] == TYPE_COVER_PY){
                            gameData[index_i][index_j] = TYPE_BOMB;
                        }else{
                            gameData[index_i][index_j] = TYPE_PATH;
                        }
                        gameData[index_i ][index_j -1] = TYPE_COVER_PY;
                        gameData[index_i ][index_j -2] = TYPE_BOX;
                    }
                    else{
                        //
                    }
                }
                else if(gameData[index_i ][index_j -1] == TYPE_BOX){
                    if(gameData[index_i ][index_j -2] == TYPE_BOMB){
                        if(gameData[index_i][index_j] == TYPE_COVER_PY){
                            gameData[index_i][index_j] = TYPE_BOMB;
                        }else{
                            gameData[index_i][index_j] = TYPE_PATH;
                        }
                        gameData[index_i ][index_j -1] = TYPE_CURRENT;
                        gameData[index_i ][index_j -2] = TYPE_COVER_BOX;
                    }
                    else if(gameData[index_i ][index_j -2] == TYPE_PATH){
                        if(gameData[index_i][index_j] == TYPE_COVER_PY){
                            gameData[index_i][index_j] = TYPE_BOMB;
                        }else{
                            gameData[index_i][index_j] = TYPE_PATH;
                        }
                        gameData[index_i ][index_j -1] = TYPE_CURRENT;
                        gameData[index_i ][index_j -2] = TYPE_BOX;
                    }
                    else{
                        //
                    }
                }
            }
            else{
                //无法移动
            }
            init(gameData);
        };

    var move_right = function () {
                var index_i = -1;
                var index_j = -1;
                //遍历这个数据矩阵，找到当前的位置
                for (var i = 0; i < gameData.length; i++) {
                    for (var j = 0; j < gameData[0].length; j++) {
                        //若当前已经到达终点，则gameData对应值为TYPE_SUCCESS，否则为TYPE_CURRENT
                        if (gameData[i][j] == TYPE_CURRENT || gameData[i][j] == TYPE_COVER_PY) {
                            index_i = i;
                            index_j = j;
                            break;
                        }
                    }
                }

                if(gameData[index_i ][ index_j + 1] != TYPE_BLOCK){
                    // is path
                    if(gameData[index_i ][index_j +1] == TYPE_PATH){
                        if(gameData[index_i][index_j] == TYPE_COVER_PY){
                            gameData[index_i][index_j] = TYPE_BOMB;
                        }else{
                            gameData[index_i][index_j] = TYPE_PATH;
                        }
                        gameData[index_i ][index_j +1] = TYPE_CURRENT;
        //                $('.current').animate({top: '-=' + single_div_size + 'px'}, 1000);//向前游动的动画
                    }
                    else if(gameData[index_i ][index_j +1] == TYPE_BOMB){
                        if(gameData[index_i][index_j] == TYPE_COVER_PY){
                            gameData[index_i][index_j] = TYPE_BOMB;
                        }else{
                            gameData[index_i][index_j] = TYPE_PATH;
                        }
                        gameData[index_i ][index_j +1] = TYPE_COVER_PY;
        //                $('.current').animate({top: '-=' + single_div_size + 'px'}, 1000);//向前游动的动画
                    }
                    else if(gameData[index_i ][index_j +1] == TYPE_COVER_BOX){
                        if(gameData[index_i ][index_j +2] == TYPE_BOMB){
                            if(gameData[index_i][index_j] == TYPE_COVER_PY){
                                gameData[index_i][index_j] = TYPE_BOMB;
                            }else{
                                gameData[index_i][index_j] = TYPE_PATH;
                            }
                            gameData[index_i ][index_j +1] = TYPE_COVER_PY;
                            gameData[index_i ][index_j +2] = TYPE_COVER_BOX;
                        }
                        else if(gameData[index_i ][index_j +2] == TYPE_PATH){
                            if(gameData[index_i][index_j] == TYPE_COVER_PY){
                                gameData[index_i][index_j] = TYPE_BOMB;
                            }else{
                                gameData[index_i][index_j] = TYPE_PATH;
                            }
                            gameData[index_i ][index_j +1] = TYPE_COVER_PY;
                            gameData[index_i ][index_j +2] = TYPE_BOX;
                        }
                        else{
                            //
                        }
                    }
                    else if(gameData[index_i ][index_j +1] == TYPE_BOX){
                        if(gameData[index_i ][index_j +2] == TYPE_BOMB){
                            if(gameData[index_i][index_j] == TYPE_COVER_PY){
                                gameData[index_i][index_j] = TYPE_BOMB;
                            }else{
                                gameData[index_i][index_j] = TYPE_PATH;
                            }
                            gameData[index_i ][index_j +1] = TYPE_CURRENT;
                            gameData[index_i ][index_j +2] = TYPE_COVER_BOX;
                        }
                        else if(gameData[index_i ][index_j +2] == TYPE_PATH){
                            if(gameData[index_i][index_j] == TYPE_COVER_PY){
                                gameData[index_i][index_j] = TYPE_BOMB;
                            }else{
                                gameData[index_i][index_j] = TYPE_PATH;
                            }
                            gameData[index_i ][index_j +1] = TYPE_CURRENT;
                            gameData[index_i ][index_j +2] = TYPE_BOX;
                        }
                        else{
                            //
                        }
                    }
                }
                else{
                    //无法移动
                }
                init(gameData);
            };

    //判断游戏结果
        var check_game = function (data) {

            //用户的评星
            var rating = 0;
            flag_success = true;
            for (var i = 0; i < data.length; i++) {
                for (var j = 0; j < data[0].length; j++) {
                    if (data[i][j] == 3 || data[i][j] == 5) {
                        flag_success = false;
                        break;
                    }
                }
            }
            if (flag_success == true) {
                //如果成功到达目的地并且用时少于1分钟，评为3星
                if(usedTime < 60000*5){
                    rating = 3;
                }
                //如果成功到达目的地并且用时少于3分钟，评为2星
                else if(usedTime < 60000*7){
                    rating = 2;
                }
                //如果成功到达目的地并且用时少于5分钟，评为1星
                else if(usedTime < 60000*10){
                    rating = 1;
                }
                window.android.showGameBoxResult(rating, "恭喜你成功了");
                // alert("恭喜你成功了,用时"+usedTime+"评星："+rating);
            }
//            else{
//                rating=-2;
////                window.android.showGameResult(rating, "没有到达目的地");
//                // alert("没有到达目的地,用时"+usedTime+"评星："+rating);
//            }
        };

    //执行小蝌蚪游动和转向的方法。参数code为生成代码，需要经过处理后方可执行。
        //目前暂不能实现旋转的动画效果
        var execute = function (code) {
            try {
                //按下运行按钮时，计时停止，计算用时
                finishTime = new Date().getTime();
                usedTime = finishTime - initTime;
                //实际执行时的代码
                var execute_code = new Array();

                //按换行符"\n"分解code
                var arr_code = code.split("\n");

                var line = 0;
                while (line < arr_code.length) {   //line是当前读取的行号
                    //若包含for循环，则根据for循环语法结构，计算循环次数，而后将复制循环体内的语句。这样就可以将循环结构转变为顺序结构
                    if (arr_code[line].indexOf("for(") != -1) {
                        var i_begin = 0;
                        var i_end = arr_code[line].substring(arr_code[line].indexOf("<") + 2, arr_code[line].indexOf("; i++"));//获取计数器上限
                        var loop_times = i_end - i_begin;

                        //从当前for循环所在行开始，找到当前for循环的结束标记
                        var end_line = line;
                        while (arr_code[end_line] != "}") {
                            end_line++;
                        }

                        //去掉第一行和最后一行，即为循环体
                        var loop_body = new Array();
                        for (var i = line + 1; i < end_line; i++) {
                            loop_body[i - line - 1] = arr_code[i];//loop_body是从下标0开始
                        }

                        //根据循环体和循环次数，将原本的for循环结构，转变为顺序结构
                        for (var i = 0; i < loop_times * loop_body.length; i++) {
                            execute_code.push(loop_body[i % loop_body.length]);
                        }
                        //从当前for循环结束的下一行开始，继续遍历
                        line = end_line + 1;
                    }
                    //for当前循环结束标记
                    else if (arr_code[line] == "}" || arr_code[line] == "\n") {
                        line++;
                    }
                    //不包含在for循环中的代码，即只执行一次的代码
                    else {
                        execute_code.push(arr_code[line]);
                        line++;
                    }
                }

                var timesRun = 0;
                //每500毫秒（0.5秒）执行一条命令
                var interval = setInterval(function () {
                    //如果执行完所有的代码，则停止定时器，并检查是否到达目的地
                    if (timesRun == execute_code.length - 1) {
                        clearInterval(interval);
                        check_game(gameData);
                    }
                    //执行代码。但是，一旦撞到障碍物，或者到达终点，则立即检查游戏结果，不再执行后续代码
                    else {
                        if ( flag_success == true) {
                            clearInterval(interval);
                            check_game(gameData);
                        }
                        else {
                            eval(execute_code[timesRun]);
                            timesRun++;
                        }
                    }
                }, 1000);
            }
            catch (e) {
                if (e !== Infinity) {
                    alert(e);
                }
            }

        };

    //--创建触摸监听，当浏览器打开页面时，触摸屏幕触发事件，进行音频播放
    document.addEventListener('touchstart', function () {
        function audioAutoPlay() {
            var audio = document.getElementById('bgm_global');
                audio.play();
        }
        audioAutoPlay();
    });

    //导出API
    this.init = init;
    this.move_up = move_up;
    this.move_down = move_down;
    this.move_left = move_left;
    this.move_right = move_right;
    this.execute = execute;
}
