<div class="search_form" >
    <form action="/search" method="POST">
        <label for="SearchHere">
            <span id="image" style="vertical-align: middle;">
             Search:
            </span>
        </label>
        <select name="filter" id="filter" style="width: 150px !important; min-width: 150px; max-width: 150px;">
            <option value="all">Everything</option>
            <option value="Type_Restaurant">Restaurant</option>
            <option value="Type_Temple">Temple</option>
            <option value="Category_JainTemple">JainTemple</option>
            <option value="Category_Indian">Indian Restaurants</option>
            <option value="Category_Vegetarian">Indian Vegetarian Restaurants</option>
            <option value="Category_Vegan">Indian Vegan Restaurants</option>
        </select>
        <input type="text" name="search" id="search" size="150" value="${search!""}" placeholder="Search for Jain Temples, Jain Restaurants, Choose dropdown to drill down...">
        <input type="submit" class="search" value="GO">
    </form>
</div>