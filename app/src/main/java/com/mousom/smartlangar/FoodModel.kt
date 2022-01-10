package com.mousom.smartlangar

class FoodModel {
    var Quantity: String? = null
    var food_image: String? = null
    var id: String? = null

    constructor() {}

    // Constructor
    constructor(Quantity: String?, food_image: String?, id: String?) {
        this.Quantity = Quantity
        this.food_image = food_image
        this.id = id
    }

    // Getter and Setter
    fun getuser_quantity(): String? {
        return Quantity
    }

    fun setuser_quantity(Quantity: String?) {
        this.Quantity = Quantity
    }

    fun getuser_image(): String? {
        return food_image
    }

    fun setuser_image(food_image: String?) {
        this.food_image = food_image
    }
}