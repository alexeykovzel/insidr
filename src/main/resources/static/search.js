function autocomplete(inputField, options) {
    let focus;

    // Show available options on input
    inputField.addEventListener("input", function (e) {
        closeAllLists();
        const input = this.value;
        if (!input) return false;
        focus = -1;

        // Calculate matches
        let matches = [];
        for (let i = 0; i < options.length; i++) {
            if (options[i].substring(0, input.length).toUpperCase() === input.toUpperCase()) {
                matches.push(options[i]);
            }
        }
        
        // Check if any matches
        if (matches.length > 0) {

            // Create autocomplete list
            let list = document.createElement("DIV");
            list.setAttribute("id", this.id + "autocomplete-list");
            list.setAttribute("class", "autocomplete-items");
            this.parentNode.appendChild(list);

            // Add matches to the list
            matches.forEach(val => {
                let option = document.createElement("DIV");
                option.innerHTML = "<strong>" + val.substring(0, input.length) + "</strong>";
                option.innerHTML += val.substring(input.length);
                option.innerHTML += "<input type='hidden' value='" + val + "'>";
                option.addEventListener("click", function (e) {
                    inputField.value = this.getElementsByTagName("input")[0].value;
                    closeAllLists();
                });
                list.appendChild(option);
            });
        }
    });

    // Focus on one of the options using keys
    inputField.addEventListener("keydown", function (e) {
        let options = document.getElementById(this.id + "autocomplete-list");
        if (options) options = options.getElementsByTagName("div");
        
        // on 'keydown'
        if (e.keyCode === 40) {
            focus++;
            addActive(options);
        }
        // on 'keyup'
        if (e.keyCode === 38) {
            focus--;
            addActive(options);
        }
        // on 'enter'
        if (e.keyCode === 13) {
            e.preventDefault();
            if (focus > -1 && options) {
                options[focus].click();
            }
        }
    });

    // Hide options if clicked elsewhere
    document.addEventListener("click", function (e) {
        closeAllLists(e.target);
    });

    // make options on focus active 
    function addActive(options) {
        if (!options) return false;
        removeActive(options);
        if (focus >= options.length) focus = 0;
        if (focus < 0) focus = (options.length - 1);
        options[focus].classList.add("autocomplete-active");
    }

    // Remove active options 
    function removeActive(options) {
        for (let i = 0; i < options.length; i++) {
            options[i].classList.remove("autocomplete-active");
        }
    }

    // Hide all option lists
    function closeAllLists(el) {
        const options = document.getElementsByClassName("autocomplete-items");
        for (let i = 0; i < options.length; i++) {
            if (el !== options[i] && el !== inputField) {
                options[i].parentNode.removeChild(options[i]);
            }
        }
    }
}