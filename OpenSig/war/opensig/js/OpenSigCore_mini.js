(function($){var pasteEventName=($.browser.msie?'paste':'input')+".mask";var iPhone=(window.orientation!=undefined);$.mask={definitions:{'9':"[0-9]",'a':"[A-Za-z]",'*':"[A-Za-z0-9]"}};$.fn.extend({caret:function(begin,end){if(this.length==0)return;if(typeof begin=='number'){end=(typeof end=='number')?end:begin;return this.each(function(){if(this.setSelectionRange){this.focus();this.setSelectionRange(begin,end)}else if(this.createTextRange){var range=this.createTextRange();range.collapse(true);range.moveEnd('character',end);range.moveStart('character',begin);range.select()}})}else{if(this[0].setSelectionRange){begin=this[0].selectionStart;end=this[0].selectionEnd}else if(document.selection&&document.selection.createRange){var range=document.selection.createRange();begin=0-range.duplicate().moveStart('character',-100000);end=begin+range.text.length}return{begin:begin,end:end}}},unmask:function(){return this.trigger("unmask")},mask:function(mask,settings){if(!mask&&this.length>0){var input=$(this[0]);var tests=input.data("tests");return $.map(input.data("buffer"),function(c,i){return tests[i]?c:null}).join('')}settings=$.extend({placeholder:"_",completed:null},settings);var defs=$.mask.definitions;var tests=[];var partialPosition=mask.length;var firstNonMaskPos=null;var len=mask.length;$.each(mask.split(""),function(i,c){if(c=='?'){len--;partialPosition=i}else if(defs[c]){tests.push(new RegExp(defs[c]));if(firstNonMaskPos==null)firstNonMaskPos=tests.length-1}else{tests.push(null)}});return this.each(function(){var input=$(this);var buffer=$.map(mask.split(""),function(c,i){if(c!='?')return defs[c]?settings.placeholder:c});var ignore=false;var focusText=input.val();input.data("buffer",buffer).data("tests",tests);function seekNext(pos){while(++pos<=len&&!tests[pos]){};return pos};function shiftL(pos){while(!tests[pos]&&--pos>=0){};for(var i=pos;i<len;i++){if(tests[i]){buffer[i]=settings.placeholder;var j=seekNext(i);if(j<len&&tests[i].test(buffer[j])){buffer[i]=buffer[j]}else break}}writeBuffer();input.caret(Math.max(firstNonMaskPos,pos))};function shiftR(pos){for(var i=pos,c=settings.placeholder;i<len;i++){if(tests[i]){var j=seekNext(i);var t=buffer[i];buffer[i]=c;if(j<len&&tests[j].test(t))c=t;else break}}};function keydownEvent(e){var pos=$(this).caret();var k=e.keyCode;ignore=(k<16||(k>16&&k<32)||(k>32&&k<41));if((pos.begin-pos.end)!=0&&(!ignore||k==8||k==46))clearBuffer(pos.begin,pos.end);if(k==8||k==46||(iPhone&&k==127)){shiftL(pos.begin+(k==46?0:-1));return false}else if(k==27){input.val(focusText);input.caret(0,checkVal());return false}};function keypressEvent(e){if(ignore){ignore=false;return(e.keyCode==8)?false:null}e=e||window.event;var k=e.charCode||e.keyCode||e.which;var pos=$(this).caret();if(e.ctrlKey||e.altKey||e.metaKey){return true}else if((k>=32&&k<=125)||k>186){var p=seekNext(pos.begin-1);if(p<len){var c=String.fromCharCode(k);if(tests[p].test(c)){shiftR(p);buffer[p]=c;writeBuffer();var next=seekNext(p);$(this).caret(next);if(settings.completed&&next==len)settings.completed.call(input)}}}return false};function clearBuffer(start,end){for(var i=start;i<end&&i<len;i++){if(tests[i])buffer[i]=settings.placeholder}};function writeBuffer(){return input.val(buffer.join('')).val()};function checkVal(allow){var test=input.val();var lastMatch=-1;for(var i=0,pos=0;i<len;i++){if(tests[i]){buffer[i]=settings.placeholder;while(pos++<test.length){var c=test.charAt(pos-1);if(tests[i].test(c)){buffer[i]=c;lastMatch=i;break}}if(pos>test.length)break}else if(buffer[i]==test[pos]&&i!=partialPosition){pos++;lastMatch=i}}if(!allow&&lastMatch+1<partialPosition){input.val("");clearBuffer(0,len)}else if(allow||lastMatch+1>=partialPosition){writeBuffer();if(!allow)input.val(input.val().substring(0,lastMatch+1))}return(partialPosition?i:firstNonMaskPos)};if(!input.attr("readonly"))input.one("unmask",function(){input.unbind(".mask").removeData("buffer").removeData("tests")}).bind("focus.mask",function(){focusText=input.val();var pos=checkVal();writeBuffer();setTimeout(function(){if(pos==mask.length)input.caret(0,pos);else input.caret(pos)},0)}).bind("blur.mask",function(){checkVal();if(input.val()!=focusText)input.change()}).bind("keydown.mask",keydownEvent).bind("keypress.mask",keypressEvent).bind(pasteEventName,function(){setTimeout(function(){input.caret(checkVal(true))},0)});checkVal()})}})})(jQuery);var encrypt={sha1:function(msg){function rotate_left(n,s){var t4=(n<<s)|(n>>>(32-s));return t4}function lsb_hex(val){var str="";var i;var vh;var vl;for(i=0;i<=6;i+=2){vh=(val>>>(i*4+4))&0x0f;vl=(val>>>(i*4))&0x0f;str+=vh.toString(16)+vl.toString(16)}return str}function cvt_hex(val){var str="";var i;var v;for(i=7;i>=0;i--){v=(val>>>(i*4))&0x0f;str+=v.toString(16)}return str}function Utf8Encode(string){string=string.replace(/\r\n/g,"\n");var utftext="";for(var n=0;n<string.length;n++){var c=string.charCodeAt(n);if(c<128){utftext+=String.fromCharCode(c)}else if((c>127)&&(c<2048)){utftext+=String.fromCharCode((c>>6)|192);utftext+=String.fromCharCode((c&63)|128)}else{utftext+=String.fromCharCode((c>>12)|224);utftext+=String.fromCharCode(((c>>6)&63)|128);utftext+=String.fromCharCode((c&63)|128)}}return utftext}var blockstart;var i,j;var W=new Array(80);var H0=0x67452301;var H1=0xEFCDAB89;var H2=0x98BADCFE;var H3=0x10325476;var H4=0xC3D2E1F0;var A,B,C,D,E;var temp;msg=Utf8Encode(msg);var msg_len=msg.length;var word_array=new Array();for(i=0;i<msg_len-3;i+=4){j=msg.charCodeAt(i)<<24|msg.charCodeAt(i+1)<<16|msg.charCodeAt(i+2)<<8|msg.charCodeAt(i+3);word_array.push(j)}switch(msg_len%4){case 0:i=0x080000000;break;case 1:i=msg.charCodeAt(msg_len-1)<<24|0x0800000;break;case 2:i=msg.charCodeAt(msg_len-2)<<24|msg.charCodeAt(msg_len-1)<<16|0x08000;break;case 3:i=msg.charCodeAt(msg_len-3)<<24|msg.charCodeAt(msg_len-2)<<16|msg.charCodeAt(msg_len-1)<<8|0x80;break}word_array.push(i);while((word_array.length%16)!=14)word_array.push(0);word_array.push(msg_len>>>29);word_array.push((msg_len<<3)&0x0ffffffff);for(blockstart=0;blockstart<word_array.length;blockstart+=16){for(i=0;i<16;i++)W[i]=word_array[blockstart+i];for(i=16;i<=79;i++)W[i]=rotate_left(W[i-3]^W[i-8]^W[i-14]^W[i-16],1);A=H0;B=H1;C=H2;D=H3;E=H4;for(i=0;i<=19;i++){temp=(rotate_left(A,5)+((B&C)|(~B&D))+E+W[i]+0x5A827999)&0x0ffffffff;E=D;D=C;C=rotate_left(B,30);B=A;A=temp}for(i=20;i<=39;i++){temp=(rotate_left(A,5)+(B^C^D)+E+W[i]+0x6ED9EBA1)&0x0ffffffff;E=D;D=C;C=rotate_left(B,30);B=A;A=temp}for(i=40;i<=59;i++){temp=(rotate_left(A,5)+((B&C)|(B&D)|(C&D))+E+W[i]+0x8F1BBCDC)&0x0ffffffff;E=D;D=C;C=rotate_left(B,30);B=A;A=temp}for(i=60;i<=79;i++){temp=(rotate_left(A,5)+(B^C^D)+E+W[i]+0xCA62C1D6)&0x0ffffffff;E=D;D=C;C=rotate_left(B,30);B=A;A=temp}H0=(H0+A)&0x0ffffffff;H1=(H1+B)&0x0ffffffff;H2=(H2+C)&0x0ffffffff;H3=(H3+D)&0x0ffffffff;H4=(H4+E)&0x0ffffffff}temp=cvt_hex(H0)+cvt_hex(H1)+cvt_hex(H2)+cvt_hex(H3)+cvt_hex(H4);return temp.toLowerCase()},policy:function(pwd){var score=0;if(pwd.length<6){return 0}score+=pwd.length*6;score+=(_checkRepetition(1,pwd).length-pwd.length)*1;score+=(_checkRepetition(2,pwd).length-pwd.length)*1;score+=(_checkRepetition(3,pwd).length-pwd.length)*1;score+=(_checkRepetition(4,pwd).length-pwd.length)*1;score+=(_checkRepetition(5,pwd).length-pwd.length)*1;score+=(_checkRepetition(6,pwd).length-pwd.length)*1;if(pwd.match(/(.*[0-9].*[0-9].*[0-9])/)){score+=5}if(pwd.match(/(.*[!,@,#,$,%,^,&,*,?,_,~].*[!,@,#,$,%,^,&,*,?,_,~])/)){score+=5}if(pwd.match(/([a-z].*[A-Z])|([A-Z].*[a-z])/)){score+=10}if(pwd.match(/([a-zA-Z])/)&&pwd.match(/([0-9])/)){score+=15}if(pwd.match(/([!,@,#,$,%,^,&,*,?,_,~])/)&&pwd.match(/([0-9])/)){score+=15}if(pwd.match(/([!,@,#,$,%,^,&,*,?,_,~])/)&&pwd.match(/([a-zA-Z])/)){score+=15}if(pwd.match(/^\w+$/)||pwd.match(/^\d+$/)){score-=10}return score;function _checkRepetition(pLen,str){var res="";for(var i=0;i<str.length;i++){var repeated=true;for(var j=0;j<pLen&&(j+i+pLen)<str.length;j++){repeated=repeated&&(str.charAt(j+i)==str.charAt(j+i+pLen))}if(j<pLen){repeated=false}if(repeated){i+=pLen-1;repeated=false}else{res+=str.charAt(i)}}return res}}};var base64={_keyStr:"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",encode:function(input){var output="";var chr1,chr2,chr3,enc1,enc2,enc3,enc4;var i=0;input=base64._utf8_encode(input);while(i<input.length){chr1=input.charCodeAt(i++);chr2=input.charCodeAt(i++);chr3=input.charCodeAt(i++);enc1=chr1>>2;enc2=((chr1&3)<<4)|(chr2>>4);enc3=((chr2&15)<<2)|(chr3>>6);enc4=chr3&63;if(isNaN(chr2)){enc3=enc4=64}else if(isNaN(chr3)){enc4=64}output=output+this._keyStr.charAt(enc1)+this._keyStr.charAt(enc2)+this._keyStr.charAt(enc3)+this._keyStr.charAt(enc4)}return output},decode:function(input){var output="";var chr1,chr2,chr3;var enc1,enc2,enc3,enc4;var i=0;input=input.replace(/[^A-Za-z0-9\+\/\=]/g,"");while(i<input.length){enc1=this._keyStr.indexOf(input.charAt(i++));enc2=this._keyStr.indexOf(input.charAt(i++));enc3=this._keyStr.indexOf(input.charAt(i++));enc4=this._keyStr.indexOf(input.charAt(i++));chr1=(enc1<<2)|(enc2>>4);chr2=((enc2&15)<<4)|(enc3>>2);chr3=((enc3&3)<<6)|enc4;output=output+String.fromCharCode(chr1);if(enc3!=64){output=output+String.fromCharCode(chr2)}if(enc4!=64){output=output+String.fromCharCode(chr3)}}output=base64._utf8_decode(output);return output},_utf8_encode:function(string){string=string.replace(/\r\n/g,"\n");var utftext="";for(var n=0;n<string.length;n++){var c=string.charCodeAt(n);if(c<128){utftext+=String.fromCharCode(c)}else if((c>127)&&(c<2048)){utftext+=String.fromCharCode((c>>6)|192);utftext+=String.fromCharCode((c&63)|128)}else{utftext+=String.fromCharCode((c>>12)|224);utftext+=String.fromCharCode(((c>>6)&63)|128);utftext+=String.fromCharCode((c&63)|128)}}return utftext},_utf8_decode:function(utftext){var string="";var i=0;var c=c1=c2=0;while(i<utftext.length){c=utftext.charCodeAt(i);if(c<128){string+=String.fromCharCode(c);i++}else if((c>191)&&(c<224)){c2=utftext.charCodeAt(i+1);string+=String.fromCharCode(((c&31)<<6)|(c2&63));i+=2}else{c2=utftext.charCodeAt(i+1);c3=utftext.charCodeAt(i+2);string+=String.fromCharCode(((c&15)<<12)|((c2&63)<<6)|(c3&63));i+=3}}return string}};