document.addEventListener(
    'DOMContentLoaded',
    function() {
        node = document.getElementById('editor-div');
        editor = new Squire(node);

        textArea = document.getElementById('post-body');
        editor.setHTML(textArea.value);

        form = document.getElementById('post-form');
        form.addEventListener(
            'submit',
            function() {
                textArea.value = DOMPurify.sanitize(editor.getHTML(), {FORBID_ATTR: ['style']});
            }, false)

        is_Draft_check = document.getElementById('is-draft-check');
        publish_btn = document.getElementById('publish-btn');
        draft_btn = document.getElementById('draft-btn');
        if(is_Draft_check.checked) {
            publish_btn.classList.add('hidden-item');
            draft_btn.classList.remove('hidden-item');
        }

        editor_selector_dropdown = document.getElementById('editor-selector-dropdown');
        editor_btn_selector = document.getElementById('editor-btn-selector');
        editor_selector_dropdown.addEventListener(
            "click",
            function() {
                editor_btn_selector.hidden = false;
            }, false)

        select_publish = document.getElementById('select-publish');
        select_draft = document.getElementById('select-draft');

        select_publish.addEventListener(
            'click',
            function() {
                is_Draft_check.checked = false;
                editor_btn_selector.hidden = true;
                publish_btn.classList.remove('hidden-item');
                draft_btn.classList.add('hidden-item');
            }, false)

        select_draft.addEventListener(
            'click',
            function() {
                  is_Draft_check.checked = true;
                  editor_btn_selector.hidden = true;
                  publish_btn.classList.add('hidden-item');
                  draft_btn.classList.remove('hidden-item');
            }, false)

    }, false)