//-------------------------------------------------------------------------//
// 汎用的な処理
//-------------------------------------------------------------------------//

// メインフォームの送信ボタンをクリックしたときの処理
function submitMainForm() {

  // メインフォームをサブミットする
  mainForm.submit();
}

// JavaScript関数でHTMLのリンククリックと同じ挙動を実現する
function redirectByUrl(url) {

  // HTMLのリンククリックと同じ挙動を実現する
  window.location.href = url;
}

//-------------------------------------------------------------------------//
// 個別の処理(TODO 多くなったらファイルを分割する)
//-------------------------------------------------------------------------//

function changeLimit(urlBase, offset) {

  // IDによりselectの値を取得する
  var limit = document.getElementById("selectLimit").value;

  // リンククリック(GETリクエスト)により画面遷移する
  redirectByUrl(urlBase + limit + offset);
}
