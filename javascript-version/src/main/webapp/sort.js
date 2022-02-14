(function () {
  /**
   * Returns the text content of a cell
   * @param  {} tr - Row Father
   * @param  {} idx - Cell Index
   */
  function getCellValue(tr, idx) {
    return tr.children[idx].textContent; // idx indexes the columns of the tr
  }

  /**
   * Function that compares ttwo elements of type integer or string
   * @param  {} idx - Cell Index
   * @param  {} asc - Boolean parameter
   */
  function createComparer(idx, asc) {
    return function (a, b) {
      var v1 = getCellValue(asc ? a : b, idx),
        v2 = getCellValue(asc ? b : a, idx);
      // If non numeric value
      if (v1 === "" || v2 === "" || isNaN(v1) || isNaN(v2)) {
        return v1.toString().localeCompare(v2); // lexical comparison
      }
      // If numeric value
      return v1 - v2; // v1 greater than v2 --> true
    };
  }

  /**
   * Function that compares ttwo elements of type integer or string
   * @param  {} idx - Cell Index
   * @param  {} asc - Boolean parameter
   */
  function createCustomComparer(idx, asc) {
    return function (a, b) {
      var sortOrder = [
          "assente",
          "rimandato",
          "riprovato",
          "18",
          "19",
          "20",
          "21",
          "22",
          "23",
          "24",
          "25",
          "26",
          "27",
          "28",
          "29",
          "30",
          "30 e lode",
        ],
        v1 = getCellValue(asc ? a : b, idx).toLowerCase(),
        v2 = getCellValue(asc ? b : a, idx).toLowerCase();
      return sortOrder.indexOf(v1) - sortOrder.indexOf(v2);
    };
  }

  /**
   * Reset all asc parameters to true
   */
  function resetAllSort() {
    document.querySelectorAll("th.sortable").forEach(function (th) {
      th.setAttribute("asc", true);
    });
    document.querySelectorAll("th.sortable_custom").forEach(function (th) {
      th.setAttribute("asc", true);
    });
  }

  resetAllSort();

  document.querySelectorAll("th.sortable").forEach(function (th) {
    // apply effect to all sortable classes
    th.addEventListener("click", function () {
      var table = th.closest("table");
      var asc = th.getAttribute("asc");
      var bool = asc == "true";
      resetAllSort();
      th.setAttribute("asc", !bool);
      Array.from(table.querySelectorAll("tbody > tr"))
        .sort(
          createComparer(Array.from(th.parentNode.children).indexOf(th), bool)
        )
        .forEach(function (tr) {
          table.querySelector("tbody").appendChild(tr);
        });
    });
  });

  document.querySelectorAll("th.sortable_custom").forEach(function (th) {
    // apply effect to all sortable_custom classes
    th.addEventListener("click", function () {
      var table = th.closest("table");
      var asc = th.getAttribute("asc");
      var bool = asc == "true";
      resetAllSort();
      th.setAttribute("asc", !bool);
      Array.from(table.querySelectorAll("tbody > tr"))
        .sort(
          createCustomComparer(
            Array.from(th.parentNode.children).indexOf(th),
            bool
          )
        )
        .forEach(function (tr) {
          table.querySelector("tbody").appendChild(tr);
        });
    });
  });
})();
