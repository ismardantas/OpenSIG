(function(f){f.fn.extend({validarCMC7:function(a){try{exp=/\D/g;a=a.toString().replace(exp,"");var b,d,c,e;b=this.modulo10(a.substring(8,18));d=this.modulo10("000"+a.substring(0,7));c=this.modulo10(a.substring(19,29));e=a.substring(0,7)+b+a.substring(8,18)+d+a.substring(19,29)+c;return a==e}catch(g){return false}},modulo10:function(a){var b=0,d=true,c;for(i=0;i<a.length;i++){c=a.substring(i,i+1);if(d)b=new Number(b)+new Number(c);else if(c>4)b=b+c*2-9;else b+=c*2;d=!d}return b%10==0?0:10-b%10}})})(jQuery);